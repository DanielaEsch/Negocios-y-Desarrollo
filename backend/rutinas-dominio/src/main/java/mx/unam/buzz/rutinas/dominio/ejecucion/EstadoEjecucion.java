package mx.unam.buzz.rutinas.dominio.ejecucion;

/**
 * Estados de una ejecucion de rutina. La secuencia es estricta: no se salta
 * ningun paso y los estados finales no tienen salida.
 *
 * <pre>
 * PROGRAMADA -> EN_CURSO -> COMPLETADA
 *     |-> CANCELADA
 *     |-> VENCIDA
 * </pre>
 */
public enum EstadoEjecucion {

    /** Generada por la frecuencia de la rutina; nadie la ha tomado. */
    PROGRAMADA,

    /** Un operador la tomo y esta trabajando sus tareas. */
    EN_CURSO,

    /** Todas sus tareas se completaron. Final. */
    COMPLETADA,

    /** Se cancelo antes de iniciar. Final. */
    CANCELADA,

    /** Paso su fecha sin que nadie la iniciara: es el incumplimiento que mide estadisticas. Final. */
    VENCIDA;

    public boolean puedePasarA(EstadoEjecucion destino) {
        return switch (this) {
            case PROGRAMADA -> destino == EN_CURSO || destino == CANCELADA || destino == VENCIDA;
            case EN_CURSO -> destino == COMPLETADA;
            case COMPLETADA, CANCELADA, VENCIDA -> false;
        };
    }
}
