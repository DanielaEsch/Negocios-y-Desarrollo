package mx.unam.buzz.pasarela;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.CargoRespuesta;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.SolicitudCargo;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.SolicitudToken;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.TarjetaResumen;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.TokenRespuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Logica del simulador. Imita el ciclo de un proveedor real:
 * <ol>
 *   <li>El navegador cambia la tarjeta por un token de un solo uso (el comercio nunca ve el numero).</li>
 *   <li>El comercio cobra con el token y su llave secreta: la respuesta es "procesando".</li>
 *   <li>Segundos despues la pasarela avisa el resultado por webhook firmado. Ese aviso es la verdad.</li>
 * </ol>
 * Todo vive en memoria: al reiniciar se pierde, igual que un sandbox que se limpia.
 */
public class PasarelaSandboxService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PasarelaSandboxService.class);
    private static final Duration VIDA_TOKEN = Duration.ofMinutes(10);
    private static final String MONEDA = "MXN";

    private record TokenGuardado(TarjetaResumen tarjeta, Optional<TarjetasDePrueba.Rechazo> rechazo, Instant expira) {
    }

    private final PasarelaSandboxProperties propiedades;
    private final EntregaWebhook entrega;
    private final ObjectMapper json;
    private final Clock reloj;
    private final ScheduledExecutorService programador;
    private final Map<String, TokenGuardado> tokens = new ConcurrentHashMap<>();
    private final Map<String, CargoRespuesta> cargos = new ConcurrentHashMap<>();

    public PasarelaSandboxService(PasarelaSandboxProperties propiedades, EntregaWebhook entrega, ObjectMapper json,
                                  Clock reloj) {
        this.propiedades = propiedades;
        this.entrega = entrega;
        this.json = json;
        this.reloj = reloj;
        this.programador = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "pasarela-sandbox-webhooks");
            hilo.setDaemon(true);
            return hilo;
        });
    }

    // ------------------------------------------------------------------ llaves

    public void exigirLlavePublica(String autorizacion) {
        exigirLlave(autorizacion, propiedades.llavePublica());
    }

    public void exigirLlaveSecreta(String autorizacion) {
        exigirLlave(autorizacion, propiedades.llaveSecreta());
    }

    private static void exigirLlave(String autorizacion, String esperada) {
        String llave = autorizacion != null && autorizacion.startsWith("Bearer ")
                ? autorizacion.substring("Bearer ".length()).trim() : null;
        if (llave == null || !llave.equals(esperada)) {
            throw new PasarelaSandboxException(HttpStatus.UNAUTHORIZED, "autenticacion", "llave_invalida",
                    "La llave de API no es valida para esta operacion");
        }
    }

    // ------------------------------------------------------------------ tokens

    public TokenRespuesta tokenizar(SolicitudToken solicitud) {
        if (solicitud == null) {
            throw PasarelaSandboxException.solicitud("datos_faltantes", "Faltan los datos de la tarjeta");
        }
        String numero = solicitud.numero() == null ? "" : solicitud.numero().replaceAll("[\\s-]", "");
        if (!TarjetasDePrueba.pasaLuhn(numero)) {
            throw tarjeta("numero_invalido", "El numero de tarjeta no es valido");
        }
        if (solicitud.mesExpiracion() == null || solicitud.anioExpiracion() == null
                || !TarjetasDePrueba.vigente(solicitud.mesExpiracion(), solicitud.anioExpiracion(),
                YearMonth.now(reloj.withZone(ZoneId.systemDefault())))) {
            throw tarjeta("tarjeta_vencida", "La fecha de expiracion no es valida o ya paso");
        }
        String marca = TarjetasDePrueba.marca(numero);
        int largoCvc = "AMEX".equals(marca) ? 4 : 3;
        if (solicitud.cvc() == null || !solicitud.cvc().matches("\\d{" + largoCvc + "}")) {
            throw tarjeta("cvc_invalido", "El codigo de seguridad debe tener " + largoCvc + " digitos");
        }
        if (solicitud.titular() == null || solicitud.titular().isBlank()) {
            throw tarjeta("titular_obligatorio", "Escribe el nombre como aparece en la tarjeta");
        }
        int anio = solicitud.anioExpiracion() < 100 ? 2000 + solicitud.anioExpiracion() : solicitud.anioExpiracion();
        TarjetaResumen resumen = new TarjetaResumen(marca, numero.substring(numero.length() - 4),
                solicitud.mesExpiracion(), anio);
        String id = "tok_" + aleatorio();
        Instant expira = reloj.instant().plus(VIDA_TOKEN);
        tokens.put(id, new TokenGuardado(resumen, TarjetasDePrueba.rechazoPara(numero), expira));
        return new TokenRespuesta(id, "token", resumen, expira);
    }

    private static PasarelaSandboxException tarjeta(String codigo, String mensaje) {
        return new PasarelaSandboxException(HttpStatus.PAYMENT_REQUIRED, "error_tarjeta", codigo, mensaje);
    }

    // ------------------------------------------------------------------ cargos

    public CargoRespuesta cobrar(SolicitudCargo solicitud) {
        if (solicitud == null || solicitud.token() == null) {
            throw PasarelaSandboxException.solicitud("token_obligatorio", "Falta el token de la tarjeta");
        }
        if (solicitud.monto() == null || solicitud.monto().signum() <= 0 || solicitud.monto().scale() > 2) {
            throw PasarelaSandboxException.solicitud("monto_invalido", "El monto debe ser positivo y en centavos");
        }
        if (!MONEDA.equals(solicitud.moneda())) {
            throw PasarelaSandboxException.solicitud("moneda_no_soportada", "El sandbox solo cobra en MXN");
        }
        if (solicitud.referencia() == null || solicitud.referencia().isBlank()) {
            throw PasarelaSandboxException.solicitud("referencia_obligatoria", "Falta la referencia del comercio");
        }
        // remove: un token sirve una sola vez
        TokenGuardado token = tokens.remove(solicitud.token());
        if (token == null || token.expira().isBefore(reloj.instant())) {
            throw PasarelaSandboxException.solicitud("token_invalido", "El token no existe, ya se uso o expiro");
        }
        CargoRespuesta cargo = new CargoRespuesta("ch_" + aleatorio(), "cargo", "procesando", solicitud.monto(),
                MONEDA, solicitud.referencia(), token.tarjeta(), null, reloj.instant());
        cargos.put(cargo.id(), cargo);
        LOG.info("[sandbox] cargo {} por {} {} (ref {}) en proceso", cargo.id(), cargo.monto(), MONEDA,
                cargo.referencia());
        programador.schedule(() -> resolver(cargo.id(), token.rechazo()), propiedades.retrasoMs(),
                TimeUnit.MILLISECONDS);
        return cargo;
    }

    public CargoRespuesta consultar(String idCargo) {
        CargoRespuesta cargo = cargos.get(idCargo);
        if (cargo == null) {
            throw new PasarelaSandboxException(HttpStatus.NOT_FOUND, "solicitud_invalida", "cargo_inexistente",
                    "No existe el cargo " + idCargo);
        }
        return cargo;
    }

    /** Decide el resultado del cargo y lo avisa por webhook. */
    void resolver(String idCargo, Optional<TarjetasDePrueba.Rechazo> rechazo) {
        CargoRespuesta previo = cargos.get(idCargo);
        CargoRespuesta resuelto = new CargoRespuesta(previo.id(), previo.objeto(),
                rechazo.isPresent() ? "fallido" : "exitoso", previo.monto(), previo.moneda(), previo.referencia(),
                previo.tarjeta(), rechazo.map(TarjetasDePrueba.Rechazo::mensaje).orElse(null), previo.creado());
        cargos.put(idCargo, resuelto);
        avisar(resuelto, rechazo.map(TarjetasDePrueba.Rechazo::codigo).orElse(null));
    }

    private void avisar(CargoRespuesta cargo, String codigoRechazo) {
        String cuerpo = evento(cargo, codigoRechazo);
        if (!propiedades.debeEntregar()) {
            LOG.info("[sandbox] entrega de webhooks apagada; evento {}", cuerpo);
            return;
        }
        programador.execute(() -> entregarConReintentos(cuerpo, 0));
        if (propiedades.duplicarWebhook()) {
            // Simula que la red reintenta: el comercio debe ignorar el segundo aviso (idempotencia)
            programador.schedule(() -> entregarConReintentos(cuerpo, 0), 700, TimeUnit.MILLISECONDS);
        }
    }

    private void entregarConReintentos(String cuerpo, int intento) {
        // Se firma en cada intento: la firma lleva el momento del envio
        String firma = FirmaWebhook.cabecera(propiedades.secretoWebhook(), reloj.instant().getEpochSecond(), cuerpo);
        if (entrega.entregar(propiedades.urlWebhook(), cuerpo, firma)) {
            return;
        }
        if (intento < propiedades.reintentos()) {
            long espera = 1000L << intento;
            programador.schedule(() -> entregarConReintentos(cuerpo, intento + 1), espera, TimeUnit.MILLISECONDS);
        } else {
            LOG.error("[sandbox] se agotaron los reintentos del webhook");
        }
    }

    String evento(CargoRespuesta cargo, String codigoRechazo) {
        Map<String, Object> tarjeta = new LinkedHashMap<>();
        tarjeta.put("marca", cargo.tarjeta().marca());
        tarjeta.put("ultimos4", cargo.tarjeta().ultimos4());

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("cargo", cargo.id());
        datos.put("referencia", cargo.referencia());
        // Como texto: un double perderia centavos
        datos.put("monto", cargo.monto().toPlainString());
        datos.put("moneda", cargo.moneda());
        datos.put("tarjeta", tarjeta);
        datos.put("motivo", cargo.motivo());
        datos.put("codigo", codigoRechazo);

        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("id", "evt_" + aleatorio());
        evento.put("tipo", "exitoso".equals(cargo.estado()) ? "cargo.exitoso" : "cargo.fallido");
        evento.put("creado", reloj.instant().getEpochSecond());
        evento.put("datos", datos);
        try {
            return json.writeValueAsString(evento);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String aleatorio() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }


    @Override
    public void close() {
        programador.shutdownNow();
    }
}
