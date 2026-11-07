package mx.unam.buzz.rutinas.dominio.rutina;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Estrategias de calendario: a que horas toca cada rutina en un dia.
 */
class FrecuenciaTest {

    /** Jueves 5 de noviembre de 2026. */
    private static final LocalDate JUEVES = LocalDate.of(2026, 11, 5);

    @Test
    void cadaDosHorasCubreLaJornadaCompleta() {
        // La revision de banos de la entrevista de campo: de 8:00 a 20:00
        List<LocalDateTime> horas = Frecuencia.CADA_2_HORAS.horarios(JUEVES,
                new Horario(LocalTime.of(8, 0), LocalTime.of(20, 0), null));

        assertEquals(7, horas.size());
        assertEquals(JUEVES.atTime(8, 0), horas.get(0));
        assertEquals(JUEVES.atTime(20, 0), horas.get(6));
    }

    @Test
    void diariaUnaVez() {
        assertEquals(List.of(JUEVES.atTime(7, 30)),
                Frecuencia.DIARIA.horarios(JUEVES, new Horario(LocalTime.of(7, 30), null, null)));
    }

    @Test
    void semanalSoloElDiaIndicado() {
        Horario lunes = new Horario(LocalTime.of(9, 0), null, DayOfWeek.MONDAY);
        assertTrue(Frecuencia.SEMANAL.horarios(JUEVES, lunes).isEmpty());
        assertEquals(1, Frecuencia.SEMANAL.horarios(LocalDate.of(2026, 11, 9), lunes).size());
    }

    @Test
    void porEventoNoSeProgramaPorCalendario() {
        assertTrue(Frecuencia.POR_EVENTO.horarios(JUEVES, new Horario(null, null, null)).isEmpty());
    }

    @Test
    void cadaFrecuenciaPideSusDatos() {
        assertThrows(ReglaNegocioException.class,
                () -> Frecuencia.CADA_2_HORAS.validar(new Horario(LocalTime.of(8, 0), null, null)));
        assertThrows(ReglaNegocioException.class,
                () -> Frecuencia.CADA_2_HORAS.validar(new Horario(LocalTime.of(20, 0), LocalTime.of(8, 0), null)));
        assertThrows(ReglaNegocioException.class,
                () -> Frecuencia.SEMANAL.validar(new Horario(LocalTime.of(9, 0), null, null)));
        assertThrows(ReglaNegocioException.class,
                () -> Frecuencia.DIARIA.validar(new Horario(null, null, null)));
        Frecuencia.POR_EVENTO.validar(new Horario(null, null, null));
    }
}
