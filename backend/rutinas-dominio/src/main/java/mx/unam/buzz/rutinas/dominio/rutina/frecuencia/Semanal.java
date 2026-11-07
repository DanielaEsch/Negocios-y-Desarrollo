package mx.unam.buzz.rutinas.dominio.rutina.frecuencia;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.rutina.Horario;
import mx.unam.buzz.rutinas.dominio.rutina.ReglaFrecuencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Una vez a la semana, el dia y la hora indicados. */
public class Semanal implements ReglaFrecuencia {

    @Override
    public List<LocalDateTime> horarios(LocalDate dia, Horario horario) {
        return dia.getDayOfWeek() == horario.diaSemana()
                ? List.of(dia.atTime(horario.horaInicio()))
                : List.of();
    }

    @Override
    public void validar(Horario horario) {
        if (horario.horaInicio() == null || horario.diaSemana() == null) {
            throw new ReglaNegocioException("Una rutina semanal necesita dia de la semana y hora de inicio");
        }
    }
}
