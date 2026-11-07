package mx.unam.buzz.rutinas.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activa las tareas programadas (programacion diaria y vencimientos). En las
 * pruebas se apaga para que el reloj no mueva datos a media prueba.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "taskin.programacion.habilitada", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
