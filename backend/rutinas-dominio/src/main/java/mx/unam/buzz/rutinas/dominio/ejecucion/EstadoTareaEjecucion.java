package mx.unam.buzz.rutinas.dominio.ejecucion;

/**
 * Estados de cada tarea dentro de una ejecucion: PENDIENTE -> EN_PROCESO -> COMPLETADA.
 */
public enum EstadoTareaEjecucion {

    PENDIENTE,
    EN_PROCESO,
    COMPLETADA;

    public boolean puedePasarA(EstadoTareaEjecucion destino) {
        return switch (this) {
            case PENDIENTE -> destino == EN_PROCESO;
            case EN_PROCESO -> destino == COMPLETADA;
            case COMPLETADA -> false;
        };
    }
}
