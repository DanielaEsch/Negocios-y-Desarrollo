package mx.unam.buzz.rutinas.dominio.pago;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * El algebra de la orden, calculada solo en el servidor:
 *
 * <pre>
 * Total = (Subtotal - Descuentos) + IVA(16 %)
 * </pre>
 *
 * <ol>
 *   <li>Subtotal: precio mensual por meses del periodo.</li>
 *   <li>Descuentos, antes de impuestos: meses de regalo del anual, cupon (porcentaje
 *       sobre lo que queda) y credito por los dias no usados del plan anterior.</li>
 *   <li>IVA: 16 % estricto sobre la base ya descontada.</li>
 * </ol>
 *
 * No hay envio: es un servicio digital. Todo se redondea a centavos, mitad hacia arriba.
 */
public record Desglose(BigDecimal precioMensual, int meses, BigDecimal subtotal, List<Descuento> descuentos,
                       BigDecimal descuento, BigDecimal base, BigDecimal iva, BigDecimal total) {

    public static final BigDecimal TASA_IVA = new BigDecimal("0.16");
    public static final String MONEDA = "MXN";

    /** Una linea de descuento con su concepto, para mostrarla en el resumen y en la factura. */
    public record Descuento(String concepto, BigDecimal monto) {
    }

    public Desglose {
        descuentos = List.copyOf(descuentos);
    }

    /**
     * @param porcentajeCupon 0 si no hay cupon; de 1 a 100
     * @param credito         credito por cambio de plan (0 si no aplica)
     */
    public static Desglose calcular(BigDecimal precioMensual, Periodo periodo, int porcentajeCupon,
                                    String cupon, BigDecimal credito) {
        if (precioMensual == null || precioMensual.signum() <= 0) {
            throw new ReglaNegocioException("El precio del plan debe ser mayor a cero");
        }
        if (periodo == null) {
            throw new ReglaNegocioException("El periodo es obligatorio");
        }
        if (porcentajeCupon < 0 || porcentajeCupon > 100) {
            throw new ReglaNegocioException("El porcentaje del cupon va de 0 a 100");
        }
        BigDecimal precio = centavos(precioMensual);
        BigDecimal subtotal = centavos(precio.multiply(BigDecimal.valueOf(periodo.getMeses())));
        List<Descuento> lineas = new ArrayList<>();
        BigDecimal restante = subtotal;

        if (periodo.getMesesDeRegalo() > 0) {
            BigDecimal regalo = centavos(precio.multiply(BigDecimal.valueOf(periodo.getMesesDeRegalo())));
            lineas.add(new Descuento("Pago anual: " + periodo.getMesesDeRegalo() + " meses de regalo", regalo));
            restante = restante.subtract(regalo);
        }
        if (porcentajeCupon > 0) {
            BigDecimal monto = centavos(restante.multiply(BigDecimal.valueOf(porcentajeCupon))
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            lineas.add(new Descuento("Cupon " + cupon + " (" + porcentajeCupon + " %)", monto));
            restante = restante.subtract(monto);
        }
        if (credito != null && credito.signum() > 0) {
            BigDecimal monto = centavos(credito.min(restante));
            lineas.add(new Descuento("Credito por los dias no usados del plan anterior", monto));
            restante = restante.subtract(monto);
        }

        BigDecimal descuento = subtotal.subtract(restante);
        BigDecimal base = restante;
        BigDecimal iva = centavos(base.multiply(TASA_IVA));
        return new Desglose(precio, periodo.getMeses(), subtotal, lineas, descuento, base, iva, base.add(iva));
    }

    public static BigDecimal centavos(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }
}
