package mx.unam.buzz.rutinas.dominio.pago;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Facturacion recurrente: prueba, renovacion, cambio de plan con prorrateo y vencimiento.
 */
class SuscripcionTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 15);
    private static final Long BASICO = 1L;
    private static final Long NEGOCIO = 2L;

    @Test
    void laPruebaDuraLosDiasIndicadosYLuegoVence() {
        Suscripcion suscripcion = Suscripcion.prueba(NEGOCIO, HOY, 14);

        assertEquals(EstadoSuscripcion.PRUEBA, suscripcion.estadoAl(HOY));
        assertEquals(14, suscripcion.diasRestantes(HOY));
        assertEquals(EstadoSuscripcion.VENCIDA, suscripcion.estadoAl(HOY.plusDays(15)));
        assertEquals(0, suscripcion.diasRestantes(HOY.plusDays(20)));
    }

    @Test
    void pagarDuranteLaPruebaArrancaHoy() {
        Suscripcion suscripcion = Suscripcion.prueba(NEGOCIO, HOY.minusDays(3), 14);
        suscripcion.activar(NEGOCIO, 1, HOY);

        assertEquals(EstadoSuscripcion.ACTIVA, suscripcion.estadoAl(HOY));
        assertEquals(HOY, suscripcion.getFechaInicio());
        assertEquals(HOY.plusMonths(1), suscripcion.getVigenciaFin());
    }

    @Test
    void renovarElMismoPlanSumaAlFinalDeLaVigencia() {
        Suscripcion suscripcion = Suscripcion.reconstituir(NEGOCIO, EstadoSuscripcion.ACTIVA,
                HOY.minusDays(10), HOY.plusDays(20));
        suscripcion.activar(NEGOCIO, 12, HOY);

        assertEquals(HOY.plusDays(20).plusMonths(12), suscripcion.getVigenciaFin());
        assertEquals(HOY.minusDays(10), suscripcion.getFechaInicio());
    }

    @Test
    void cambiarDePlanDaCreditoPorLosDiasNoUsados() {
        Suscripcion suscripcion = Suscripcion.reconstituir(BASICO, EstadoSuscripcion.ACTIVA,
                HOY.minusDays(15), HOY.plusDays(15));

        // 599 / 30 x 15 dias = 299.50
        assertEquals(new BigDecimal("299.50"), suscripcion.creditoPorCambio(NEGOCIO, new BigDecimal("599"), HOY));
        assertEquals(BigDecimal.ZERO, suscripcion.creditoPorCambio(BASICO, new BigDecimal("599"), HOY));

        suscripcion.activar(NEGOCIO, 1, HOY);
        assertEquals(NEGOCIO, suscripcion.getIdPlan());
        assertEquals(HOY, suscripcion.getFechaInicio());
    }

    @Test
    void laPruebaNoGeneraCredito() {
        Suscripcion suscripcion = Suscripcion.prueba(NEGOCIO, HOY, 14);

        assertEquals(BigDecimal.ZERO, suscripcion.creditoPorCambio(BASICO, new BigDecimal("1490"), HOY));
    }

    @Test
    void reactivarUnaSuscripcionVencidaArrancaDeNuevo() {
        Suscripcion suscripcion = Suscripcion.reconstituir(NEGOCIO, EstadoSuscripcion.ACTIVA,
                HOY.minusMonths(2), HOY.minusDays(5));
        assertEquals(EstadoSuscripcion.VENCIDA, suscripcion.estadoAl(HOY));

        suscripcion.activar(NEGOCIO, 1, HOY);
        assertEquals(EstadoSuscripcion.ACTIVA, suscripcion.estadoAl(HOY));
        assertEquals(HOY.plusMonths(1), suscripcion.getVigenciaFin());
    }
}
