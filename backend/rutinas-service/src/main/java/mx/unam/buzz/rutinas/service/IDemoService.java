package mx.unam.buzz.rutinas.service;

import java.time.LocalDateTime;

/**
 * Datos de demostracion: simula lo que hizo el equipo en los ultimos dias para que
 * el tablero y las incidencias tengan historia. Solo corre con taskin.demo.habilitada.
 */
public interface IDemoService {

    /** Simula los dias que aun no tienen rondas, de hoy menos diasHistorial hasta ahora. */
    void simular(int diasHistorial, LocalDateTime ahora);
}
