package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.facade.IProgramacionFacade;
import mx.unam.buzz.rutinas.service.IDemoService;
import mx.unam.buzz.rutinas.service.IEjecucionService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Las llaman las tareas programadas; corren como el usuario del sistema.
 */
@Component
@RequiredArgsConstructor
public class ProgramacionFacadeImpl implements IProgramacionFacade {

    private final IEjecucionService ejecucionService;
    private final IDemoService demoService;

    @Override
    public int programarDia(LocalDate dia) {
        return ejecucionService.programarDia(dia, dia.atStartOfDay());
    }

    @Override
    public int marcarVencidas(LocalDateTime ahora) {
        return ejecucionService.marcarVencidas(ahora);
    }

    @Override
    public void simularDemo(int diasHistorial, LocalDateTime ahora) {
        demoService.simular(diasHistorial, ahora);
    }
}
