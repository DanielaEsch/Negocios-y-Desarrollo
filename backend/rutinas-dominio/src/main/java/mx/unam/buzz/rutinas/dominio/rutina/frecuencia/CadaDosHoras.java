package mx.unam.buzz.rutinas.dominio.rutina.frecuencia;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.rutina.Horario;
import mx.unam.buzz.rutinas.dominio.rutina.ReglaFrecuencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Una vez cada dos horas entre la hora de inicio y la de fin, ambas incluidas. */
public class CadaDosHoras implements ReglaFrecuencia {

    private static final int INTERVALO_HORAS = 2;

    @Override
    public List<LocalDateTime> horarios(LocalDate dia, Horario horario) {
        List<LocalDateTime> horas = new ArrayList<>();
        LocalTime hora = horario.horaInicio();
        while (!hora.isAfter(horario.horaFin())) {
            horas.add(dia.atTime(hora));
            LocalTime siguiente = hora.plusHours(INTERVALO_HORAS);
            if (siguiente.isBefore(hora)) {
                break; // paso la medianoche
            }
            hora = siguiente;
        }
        return horas;
    }

    @Override
    public void validar(Horario horario) {
        if (horario.horaInicio() == null || horario.horaFin() == null) {
            throw new ReglaNegocioException("Una rutina cada 2 horas necesita hora de inicio y de fin");
        }
        if (!horario.horaFin().isAfter(horario.horaInicio())) {
            throw new ReglaNegocioException("La hora de fin debe ser posterior a la de inicio");
        }
    }
}
