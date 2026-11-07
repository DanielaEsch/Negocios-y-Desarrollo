package mx.unam.buzz.rutinas.dominio.rutina.frecuencia;

import mx.unam.buzz.rutinas.dominio.rutina.Horario;
import mx.unam.buzz.rutinas.dominio.rutina.ReglaFrecuencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** No va por calendario: la ejecucion se crea cuando ocurre el hecho (por ejemplo, un derrame). */
public class PorEvento implements ReglaFrecuencia {

    @Override
    public List<LocalDateTime> horarios(LocalDate dia, Horario horario) {
        return List.of();
    }

    @Override
    public void validar(Horario horario) {
        // No necesita datos de calendario.
    }
}
