package mx.unam.buzz.rutinas.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fachada de los procesos sin usuario: programacion diaria, vencimientos y demo.
 */
public interface IProgramacionFacade {

    int programarDia(LocalDate dia);

    int marcarVencidas(LocalDateTime ahora);

    void simularDemo(int diasHistorial, LocalDateTime ahora);
}
