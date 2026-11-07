package mx.unam.buzz.rutinas.dominio.rutina;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Datos de calendario de una rutina. Cada frecuencia usa los que necesita:
 * CADA_2_HORAS usa inicio y fin, DIARIA solo inicio, SEMANAL inicio y dia, y
 * POR_EVENTO ninguno.
 */
public record Horario(LocalTime horaInicio, LocalTime horaFin, DayOfWeek diaSemana) {
}
