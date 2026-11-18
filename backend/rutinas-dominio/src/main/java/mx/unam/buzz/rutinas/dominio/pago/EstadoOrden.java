package mx.unam.buzz.rutinas.dominio.pago;

/**
 * Ciclo de vida de una orden de pago. Lo mueven solo la pasarela (webhook firmado)
 * o Cobranza (transferencia validada); el navegador nunca decide que algo se pago.
 *
 * <pre>
 * PENDIENTE_PAGO -> PROCESANDO -> PAGADO
 *       |                |-> RECHAZADO
 *       |-> VENCIDO
 * </pre>
 */
public enum EstadoOrden {

    /** La orden existe con su total calculado por el servidor; nadie ha pagado. */
    PENDIENTE_PAGO,

    /** La pasarela recibio el token o el cliente reporto su transferencia; falta la confirmacion. */
    PROCESANDO,

    /** Confirmado por la fuente de verdad. Final: aqui nace la factura. */
    PAGADO,

    /** La pasarela declino el cargo o Cobranza no encontro el deposito. Final. */
    RECHAZADO,

    /** Nadie pago antes de la fecha limite. Final. */
    VENCIDO;

    public boolean puedePasarA(EstadoOrden destino) {
        return switch (this) {
            case PENDIENTE_PAGO -> destino == PROCESANDO || destino == VENCIDO;
            case PROCESANDO -> destino == PAGADO || destino == RECHAZADO;
            case PAGADO, RECHAZADO, VENCIDO -> false;
        };
    }

    public boolean esFinal() {
        return this == PAGADO || this == RECHAZADO || this == VENCIDO;
    }
}
