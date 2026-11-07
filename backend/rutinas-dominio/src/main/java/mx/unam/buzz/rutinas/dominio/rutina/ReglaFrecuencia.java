package mx.unam.buzz.rutinas.dominio.rutina;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Estrategia de calendario (patron Strategy): cada frecuencia sabe a que horas
 * toca su rutina en un dia dado y que datos necesita para saberlo. Agregar una
 * frecuencia nueva es agregar una clase, no otro caso a un switch.
 */
public interface ReglaFrecuencia {

    /** Horas en las que hay que ejecutar la rutina ese dia, en orden. */
    List<LocalDateTime> horarios(LocalDate dia, Horario horario);

    /** Lanza ReglaNegocioException si al horario le falta algo que esta frecuencia necesita. */
    void validar(Horario horario);
}
