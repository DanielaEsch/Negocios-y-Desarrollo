package mx.unam.buzz.rutinas.common.vo.pago;

/**
 * Resumen calculado por el servidor antes de pagar.
 */
public record CotizacionVO(PlanVO plan, String periodo, String cupon, DesgloseVO desglose) {
}
