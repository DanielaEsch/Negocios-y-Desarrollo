package mx.unam.buzz.rutinas.dominio.rutina.frecuencia;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.rutina.Horario;
import mx.unam.buzz.rutinas.dominio.rutina.ReglaFrecuencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Una vez al dia, a la hora de inicio. */
public class Diaria implements ReglaFrecuencia {

    @Override
    public List<LocalDateTime> horarios(LocalDate dia, Horario horario) {
        return List.of(dia.atTime(horario.horaInicio()));
    }

    @Override
    public void validar(Horario horario) {
        if (horario.horaInicio() == null) {
            throw new ReglaNegocioException("Una rutina diaria necesita hora de inicio");
        }
    }
}
