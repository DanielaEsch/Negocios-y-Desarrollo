package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Plan a la venta. Los precios son sin IVA.
 */
public record PlanVO(
        @Schema(example = "2") Long id,
        @Schema(example = "NEGOCIO") String clave,
        @Schema(example = "Negocio") String nombre,
        @Schema(example = "Todo lo basico mas evidencias con foto y tablero del supervisor") String descripcion,
        @Schema(example = "499.00") BigDecimal precioMensual,
        @Schema(description = "12 meses con 2 de regalo", example = "4990.00") BigDecimal precioAnual,
        @Schema(description = "true si es el plan de la suscripcion actual") boolean actual) {
}
