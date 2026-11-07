package mx.unam.buzz.rutinas.dominio.rutina;

import mx.unam.buzz.rutinas.dominio.rutina.frecuencia.CadaDosHoras;
import mx.unam.buzz.rutinas.dominio.rutina.frecuencia.Diaria;
import mx.unam.buzz.rutinas.dominio.rutina.frecuencia.PorEvento;
import mx.unam.buzz.rutinas.dominio.rutina.frecuencia.Semanal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Catalogo de frecuencias. Cada valor delega en su estrategia de calendario;
 * el enum solo las nombra (es lo que se guarda en la base).
 */
public enum Frecuencia {

    CADA_2_HORAS(new CadaDosHoras()),
    DIARIA(new Diaria()),
    SEMANAL(new Semanal()),
    POR_EVENTO(new PorEvento());

    private final ReglaFrecuencia regla;

    Frecuencia(ReglaFrecuencia regla) {
        this.regla = regla;
    }

    public List<LocalDateTime> horarios(LocalDate dia, Horario horario) {
        return regla.horarios(dia, horario);
    }

    public void validar(Horario horario) {
        regla.validar(horario);
    }
}
