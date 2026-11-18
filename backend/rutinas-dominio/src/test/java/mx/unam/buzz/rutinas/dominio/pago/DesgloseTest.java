package mx.unam.buzz.rutinas.dominio.pago;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El algebra de la orden: Total = (Subtotal - Descuentos) + IVA(16 %). En el modulo
 * financiero, si no esta cubierto por una prueba, asumimos que esta roto.
 */
class DesgloseTest {

    private static BigDecimal pesos(String monto) {
        return new BigDecimal(monto);
    }

    @Test
    void cienMenosVeintePorCientoMasIvaEsNoventaYDosOchenta() {
        // El caso de la clase: 100 - 20 % + 16 % = 92.80
        Desglose desglose = Desglose.calcular(pesos("100"), Periodo.MENSUAL, 20, "BUZZ20", BigDecimal.ZERO);

        assertEquals(pesos("100.00"), desglose.subtotal());
        assertEquals(pesos("20.00"), desglose.descuento());
        assertEquals(pesos("80.00"), desglose.base());
        assertEquals(pesos("12.80"), desglose.iva());
        assertEquals(pesos("92.80"), desglose.total());
    }

    @Test
    void mensualSinDescuentosSoloSumaIva() {
        Desglose desglose = Desglose.calcular(pesos("1490"), Periodo.MENSUAL, 0, null, null);

        assertEquals(pesos("1490.00"), desglose.subtotal());
        assertEquals(pesos("0.00"), desglose.descuento());
        assertEquals(pesos("238.40"), desglose.iva());
        assertEquals(pesos("1728.40"), desglose.total());
        assertTrue(desglose.descuentos().isEmpty());
    }

    @Test
    void anualRegalaDosMesesAntesDeImpuestos() {
        Desglose desglose = Desglose.calcular(pesos("1490"), Periodo.ANUAL, 0, null, null);

        assertEquals(12, desglose.meses());
        assertEquals(pesos("17880.00"), desglose.subtotal());
        assertEquals(pesos("2980.00"), desglose.descuento());
        assertEquals(pesos("14900.00"), desglose.base());
        assertEquals(pesos("2384.00"), desglose.iva());
        assertEquals(pesos("17284.00"), desglose.total());
    }

    @Test
    void elCuponSeAplicaSobreLoQueQuedaDespuesDelRegaloAnual() {
        Desglose desglose = Desglose.calcular(pesos("599"), Periodo.ANUAL, 20, "BUZZ20", null);

        // 599 x 12 = 7188; regalo 1198; cupon 20 % de 5990 = 1198; base 4792; IVA 766.72
        assertEquals(2, desglose.descuentos().size());
        assertEquals(pesos("2396.00"), desglose.descuento());
        assertEquals(pesos("4792.00"), desglose.base());
        assertEquals(pesos("766.72"), desglose.iva());
        assertEquals(pesos("5558.72"), desglose.total());
    }

    @Test
    void elIvaSeRedondeaACentavosMitadHaciaArriba() {
        // base 0.03 x 16 % = 0.0048 -> 0.00; base 33.33 x 16 % = 5.3328 -> 5.33
        assertEquals(pesos("5.33"), Desglose.calcular(pesos("33.33"), Periodo.MENSUAL, 0, null, null).iva());
        // 10.03 x 16 % = 1.6048 -> 1.60; 10.05 x 16 % = 1.608 -> 1.61
        assertEquals(pesos("1.61"), Desglose.calcular(pesos("10.05"), Periodo.MENSUAL, 0, null, null).iva());
    }

    @Test
    void elCreditoNuncaDejaLaBaseEnNegativo() {
        Desglose desglose = Desglose.calcular(pesos("599"), Periodo.MENSUAL, 0, null, pesos("5000"));

        assertEquals(pesos("0.00"), desglose.base());
        assertEquals(pesos("0.00"), desglose.total());
    }

    @Test
    void precioEnCeroOCuponFueraDeRangoFallan() {
        assertThrows(ReglaNegocioException.class,
                () -> Desglose.calcular(BigDecimal.ZERO, Periodo.MENSUAL, 0, null, null));
        assertThrows(ReglaNegocioException.class,
                () -> Desglose.calcular(pesos("100"), Periodo.MENSUAL, 120, "MAL", null));
    }
}
