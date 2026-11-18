package mx.unam.buzz.rutinas.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.unam.buzz.rutinas.facade.IProgramacionFacade;
import mx.unam.buzz.rutinas.service.IPagoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reloj del sistema: al arrancar y cada medianoche programa las rondas del dia, y
 * cada cinco minutos marca como vencidas las que nadie tomo a tiempo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "taskin.programacion.habilitada", havingValue = "true", matchIfMissing = true)
public class ProgramacionJobs {

    private final IProgramacionFacade programacionFacade;
    private final IPagoService pagoService;

    @Value("${taskin.demo.habilitada:false}")
    private boolean demo;

    @Value("${taskin.demo.dias-historial:6}")
    private int diasHistorial;

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        if (demo) {
            log.info("Modo demo: simulando los ultimos {} dias", diasHistorial);
            programacionFacade.simularDemo(diasHistorial, LocalDateTime.now());
        }
        programacionFacade.programarDia(LocalDate.now());
        programacionFacade.marcarVencidas(LocalDateTime.now());
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void programarHoy() {
        programacionFacade.programarDia(LocalDate.now());
    }

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT5M")
    public void marcarVencidas() {
        programacionFacade.marcarVencidas(LocalDateTime.now());
    }

    /** Ordenes de pago que nadie pago a tiempo (tarjeta 30 min, transferencia 72 h). */
    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
    public void vencerOrdenesDePago() {
        pagoService.vencerOrdenes(LocalDateTime.now());
    }
}
