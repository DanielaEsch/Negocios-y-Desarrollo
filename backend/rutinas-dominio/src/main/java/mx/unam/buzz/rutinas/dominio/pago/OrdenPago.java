package mx.unam.buzz.rutinas.dominio.pago;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.comun.TransicionInvalidaException;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lo que el negocio cliente le compra a TaskIn: un plan por un periodo, con su total
 * calculado aqui (nunca por el frontend) y una maquina de estados que solo avanzan
 * las fuentes de verdad:
 * <ul>
 *   <li>Tarjeta: el webhook firmado de la pasarela confirma o rechaza.</li>
 *   <li>Transferencia: Cobranza valida el deposito.</li>
 * </ul>
 *
 * De la tarjeta solo se guarda lo que PCI-DSS permite: marca y ultimos 4 digitos.
 */
public class OrdenPago {

    private final Long idPlan;
    private final Periodo periodo;
    private final MetodoPago metodo;
    private final Desglose desglose;
    private final String cupon;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaExpiracion;
    private EstadoOrden estado;
    private String referenciaPago;
    private String marcaTarjeta;
    private String ultimos4;
    private String motivoRechazo;
    private Long idValidador;
    private LocalDateTime fechaPago;

    private OrdenPago(Long idPlan, Periodo periodo, MetodoPago metodo, Desglose desglose, String cupon,
                      LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion) {
        this.idPlan = idPlan;
        this.periodo = periodo;
        this.metodo = metodo;
        this.desglose = desglose;
        this.cupon = cupon;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
    }

    /** Crea la orden en PENDIENTE_PAGO con una fecha limite que depende del metodo. */
    public static OrdenPago crear(Long idPlan, Periodo periodo, MetodoPago metodo, Desglose desglose, String cupon,
                                  LocalDateTime ahora) {
        validar(idPlan != null && idPlan > 0, "El plan no es valido");
        validar(periodo != null, "El periodo es obligatorio");
        validar(metodo != null, "El metodo de pago es obligatorio");
        validar(desglose != null && desglose.total().signum() > 0, "La orden necesita un total mayor a cero");
        validar(ahora != null, "La fecha de creacion es obligatoria");

        OrdenPago orden = new OrdenPago(idPlan, periodo, metodo, desglose, cupon, ahora,
                ahora.plus(metodo.getVigencia()));
        orden.estado = EstadoOrden.PENDIENTE_PAGO;
        return orden;
    }

    /** Rehidrata una orden guardada para aplicarle las reglas. */
    public static OrdenPago reconstituir(Long idPlan, Periodo periodo, MetodoPago metodo, Desglose desglose,
                                         String cupon, LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion,
                                         EstadoOrden estado, String referenciaPago, String marcaTarjeta,
                                         String ultimos4, String motivoRechazo, Long idValidador,
                                         LocalDateTime fechaPago) {
        OrdenPago orden = new OrdenPago(idPlan, periodo, metodo, desglose, cupon, fechaCreacion, fechaExpiracion);
        orden.estado = estado;
        orden.referenciaPago = referenciaPago;
        orden.marcaTarjeta = marcaTarjeta;
        orden.ultimos4 = ultimos4;
        orden.motivoRechazo = motivoRechazo;
        orden.idValidador = idValidador;
        orden.fechaPago = fechaPago;
        return orden;
    }

    /** El token de la tarjeta va a la pasarela: la orden queda esperando su respuesta. */
    public void iniciarCobroConTarjeta(LocalDateTime ahora) {
        exigirMetodo(MetodoPago.TARJETA);
        exigirVigente(ahora);
        cambiarEstado(EstadoOrden.PROCESANDO);
    }

    /** La pasarela acepto el token y abrio un cargo: se guarda su id y lo permitido de la tarjeta. */
    public void registrarCargo(String idCargo, String marca, String ultimos4) {
        exigirMetodo(MetodoPago.TARJETA);
        validar(estado == EstadoOrden.PROCESANDO, "Solo una orden en PROCESANDO registra un cargo");
        validar(idCargo != null && !idCargo.isBlank(), "El cargo de la pasarela no es valido");
        validar(ultimos4 == null || ultimos4.matches("\\d{4}"),
                "De la tarjeta solo se guardan los ultimos 4 digitos");
        this.referenciaPago = idCargo;
        this.marcaTarjeta = marca;
        this.ultimos4 = ultimos4;
    }

    /** Modelo B2B: el cliente avisa que ya transfirio y da la clave de rastreo del SPEI. */
    public void reportarTransferencia(String claveRastreo, LocalDateTime ahora) {
        exigirMetodo(MetodoPago.TRANSFERENCIA);
        validar(claveRastreo != null && !claveRastreo.isBlank(), "La clave de rastreo es obligatoria");
        exigirVigente(ahora);
        cambiarEstado(EstadoOrden.PROCESANDO);
        this.referenciaPago = claveRastreo.trim().toUpperCase();
    }

    /**
     * La pasarela confirma el cobro (webhook). Si el monto notificado no es el total que
     * calculamos, no se acepta: alguien altero algo en el camino.
     */
    public void confirmarPorPasarela(BigDecimal montoCobrado, LocalDateTime ahora) {
        exigirMetodo(MetodoPago.TARJETA);
        validar(montoCobrado != null && Desglose.centavos(montoCobrado).compareTo(desglose.total()) == 0,
                "El monto cobrado no coincide con el total de la orden");
        cambiarEstado(EstadoOrden.PAGADO);
        this.fechaPago = ahora;
    }

    /** Modelo B2B: Cobranza encontro el deposito. Es la aprobacion que sustituye al webhook. */
    public void validarTransferencia(Long idValidador, Perfil perfil, LocalDateTime ahora) {
        exigirMetodo(MetodoPago.TRANSFERENCIA);
        validar(perfil == Perfil.COBRANZA, "Solo Cobranza de TaskIn valida transferencias");
        validar(idValidador != null && idValidador > 0, "El validador no es valido");
        cambiarEstado(EstadoOrden.PAGADO);
        this.idValidador = idValidador;
        this.fechaPago = ahora;
    }

    /** La pasarela declino el cargo o Cobranza no encontro el deposito. */
    public void rechazar(String motivo) {
        validar(motivo != null && !motivo.isBlank(), "Para rechazar un pago hay que decir por que");
        cambiarEstado(EstadoOrden.RECHAZADO);
        this.motivoRechazo = motivo.trim();
    }

    /** Pasa a VENCIDO una orden que nadie pago a tiempo. */
    public void vencer(LocalDateTime ahora) {
        validar(estaExpirada(ahora), "La orden todavia esta a tiempo de pagarse");
        cambiarEstado(EstadoOrden.VENCIDO);
    }

    public boolean estaExpirada(LocalDateTime ahora) {
        return ahora != null && ahora.isAfter(fechaExpiracion);
    }

    public Long getIdPlan() {
        return idPlan;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public Desglose getDesglose() {
        return desglose;
    }

    public String getCupon() {
        return cupon;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public String getMarcaTarjeta() {
        return marcaTarjeta;
    }

    public String getUltimos4() {
        return ultimos4;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public Long getIdValidador() {
        return idValidador;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    private void exigirMetodo(MetodoPago esperado) {
        validar(metodo == esperado, "Esta operacion es solo para pagos con " + esperado);
    }

    private void exigirVigente(LocalDateTime ahora) {
        validar(!estaExpirada(ahora), "La orden ya vencio: genera una nueva");
    }

    private void cambiarEstado(EstadoOrden destino) {
        if (!estado.puedePasarA(destino)) {
            throw new TransicionInvalidaException("OrdenPago", estado, destino);
        }
        estado = destino;
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
