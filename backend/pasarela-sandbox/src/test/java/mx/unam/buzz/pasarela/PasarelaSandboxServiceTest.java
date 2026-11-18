package mx.unam.buzz.pasarela;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.CargoRespuesta;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.SolicitudCargo;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.SolicitudToken;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.TokenRespuesta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ciclo del simulador: token de un solo uso, cargo "procesando" y webhook firmado.
 */
class PasarelaSandboxServiceTest {

    private static final String SECRETO = "whsec_prueba";
    private static final int ANIO = YearMonth.now().getYear() + 2;

    private record Aviso(String cuerpo, String firma) {
    }

    private final BlockingQueue<Aviso> avisos = new LinkedBlockingQueue<>();
    private final ObjectMapper json = new ObjectMapper();
    private PasarelaSandboxService servicio;

    private PasarelaSandboxService crear(EntregaWebhook entrega, boolean duplicar) {
        PasarelaSandboxProperties propiedades = new PasarelaSandboxProperties(true, "pk_test", "sk_test", SECRETO,
                "http://comercio/webhook", 0, 2, duplicar, true);
        servicio = new PasarelaSandboxService(propiedades, entrega, json, Clock.systemUTC());
        return servicio;
    }

    private PasarelaSandboxService crear() {
        return crear((url, cuerpo, firma) -> avisos.add(new Aviso(cuerpo, firma)), false);
    }

    @AfterEach
    void cerrar() {
        if (servicio != null) {
            servicio.close();
        }
    }

    private static SolicitudToken tarjeta(String numero) {
        return new SolicitudToken(numero, 12, ANIO, "123", "Laura Mendez");
    }

    private static SolicitudCargo cargo(String token) {
        return new SolicitudCargo(token, new BigDecimal("1728.40"), "MXN", "TK-261115-AB12", "Plan Negocio");
    }

    @Test
    void elTokenSoloMuestraMarcaYUltimosCuatro() {
        TokenRespuesta token = crear().tokenizar(tarjeta("4242 4242 4242 4242"));

        assertTrue(token.id().startsWith("tok_"));
        assertEquals("VISA", token.tarjeta().marca());
        assertEquals("4242", token.tarjeta().ultimos4());
        assertFalse(token.toString().contains("4242424242424242"));
    }

    @Test
    void unCargoAprobadoAvisaConWebhookFirmado() throws Exception {
        PasarelaSandboxService pasarela = crear();
        CargoRespuesta cargo = pasarela.cobrar(cargo(pasarela.tokenizar(tarjeta("4242424242424242")).id()));
        assertEquals("procesando", cargo.estado());

        Aviso aviso = avisos.poll(5, TimeUnit.SECONDS);
        assertNotNull(aviso);
        JsonNode evento = json.readTree(aviso.cuerpo());
        assertEquals("cargo.exitoso", evento.get("tipo").asText());
        assertEquals("TK-261115-AB12", evento.at("/datos/referencia").asText());
        assertEquals("1728.40", evento.at("/datos/monto").asText());

        String timestamp = aviso.firma().split(",")[0].substring(2);
        String esperada = FirmaWebhook.hmac(SECRETO, timestamp + "." + aviso.cuerpo());
        assertEquals("v1=" + esperada, aviso.firma().split(",")[1]);
    }

    @Test
    void laTarjetaDeclinadaSeAvisaComoFallida() throws Exception {
        PasarelaSandboxService pasarela = crear();
        pasarela.cobrar(cargo(pasarela.tokenizar(tarjeta("4000000000000002")).id()));

        JsonNode evento = json.readTree(avisos.poll(5, TimeUnit.SECONDS).cuerpo());
        assertEquals("cargo.fallido", evento.get("tipo").asText());
        assertEquals("tarjeta_declinada", evento.at("/datos/codigo").asText());
    }

    @Test
    void elTokenNoSePuedeUsarDosVeces() {
        PasarelaSandboxService pasarela = crear();
        String token = pasarela.tokenizar(tarjeta("4242424242424242")).id();
        pasarela.cobrar(cargo(token));

        PasarelaSandboxException ex = assertThrows(PasarelaSandboxException.class,
                () -> pasarela.cobrar(cargo(token)));
        assertEquals("token_invalido", ex.getCodigo());
    }

    @Test
    void rechazaTarjetasMalEscritasAntesDeCrearElToken() {
        PasarelaSandboxService pasarela = crear();

        assertEquals("numero_invalido", assertThrows(PasarelaSandboxException.class,
                () -> pasarela.tokenizar(tarjeta("4242424242424241"))).getCodigo());
        assertEquals("tarjeta_vencida", assertThrows(PasarelaSandboxException.class,
                () -> pasarela.tokenizar(new SolicitudToken("4242424242424242", 1, 2020, "123", "X"))).getCodigo());
        assertEquals("cvc_invalido", assertThrows(PasarelaSandboxException.class,
                () -> pasarela.tokenizar(new SolicitudToken("4242424242424242", 12, ANIO, "12", "X"))).getCodigo());
    }

    @Test
    void soloAceptaSusLlaves() {
        PasarelaSandboxService pasarela = crear();

        pasarela.exigirLlavePublica("Bearer pk_test");
        assertThrows(PasarelaSandboxException.class, () -> pasarela.exigirLlaveSecreta("Bearer pk_test"));
        assertThrows(PasarelaSandboxException.class, () -> pasarela.exigirLlaveSecreta(null));
    }

    @Test
    void reintentaSiElComercioNoResponde() throws Exception {
        AtomicInteger intentos = new AtomicInteger();
        PasarelaSandboxService pasarela = crear((url, cuerpo, firma) -> {
            if (intentos.incrementAndGet() < 2) {
                return false;
            }
            return avisos.add(new Aviso(cuerpo, firma));
        }, false);
        pasarela.cobrar(cargo(pasarela.tokenizar(tarjeta("4242424242424242")).id()));

        assertNotNull(avisos.poll(5, TimeUnit.SECONDS));
        assertEquals(2, intentos.get());
    }

    @Test
    void conDuplicadoElMismoEventoLlegaDosVeces() throws Exception {
        PasarelaSandboxService pasarela = crear((url, cuerpo, firma) -> avisos.add(new Aviso(cuerpo, firma)), true);
        pasarela.cobrar(cargo(pasarela.tokenizar(tarjeta("4242424242424242")).id()));

        String primero = json.readTree(avisos.poll(5, TimeUnit.SECONDS).cuerpo()).get("id").asText();
        String segundo = json.readTree(avisos.poll(5, TimeUnit.SECONDS).cuerpo()).get("id").asText();
        assertEquals(primero, segundo);
        assertNull(avisos.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void luhnYMarcas() {
        assertTrue(TarjetasDePrueba.pasaLuhn("4242424242424242"));
        assertTrue(TarjetasDePrueba.pasaLuhn("5555555555554444"));
        assertFalse(TarjetasDePrueba.pasaLuhn("1234"));
        assertEquals(List.of("VISA", "MASTERCARD", "AMEX"), List.of(TarjetasDePrueba.marca("4242424242424242"),
                TarjetasDePrueba.marca("5555555555554444"), TarjetasDePrueba.marca("378282246310005")));
    }
}
