package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import mx.unam.buzz.rutinas.dominio.pago.Desglose;

import java.math.BigDecimal;
import java.util.List;

/**
 * Algebra de la orden: Total = (Subtotal - Descuentos) + IVA.
 */
public record DesgloseVO(
        @Schema(example = "1490.00") BigDecimal precioMensual,
        @Schema(example = "1") int meses,
        @Schema(example = "1490.00") BigDecimal subtotal,
        List<LineaDescuentoVO> descuentos,
        @Schema(example = "298.00") BigDecimal descuento,
        @Schema(example = "1192.00") BigDecimal base,
        @Schema(example = "190.72") BigDecimal iva,
        @Schema(example = "1382.72") BigDecimal total,
        @Schema(example = "MXN") String moneda) {

    public record LineaDescuentoVO(String concepto, BigDecimal monto) {
    }

    public static DesgloseVO de(Desglose desglose) {
        return new DesgloseVO(desglose.precioMensual(), desglose.meses(), desglose.subtotal(),
                desglose.descuentos().stream().map(d -> new LineaDescuentoVO(d.concepto(), d.monto())).toList(),
                desglose.descuento(), desglose.base(), desglose.iva(), desglose.total(), Desglose.MONEDA);
    }
}
