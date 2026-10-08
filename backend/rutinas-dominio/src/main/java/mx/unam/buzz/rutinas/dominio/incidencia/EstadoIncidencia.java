package mx.unam.buzz.rutinas.dominio.incidencia;

/**
 * Estados de una incidencia: PENDIENTE -> REVISION -> RESUELTA. Sin saltos ni
 * vuelta atras: una incidencia resuelta no se reabre, se reporta una nueva.
 */
public enum EstadoIncidencia {

    PENDIENTE,
    REVISION,
    RESUELTA;

    public boolean puedePasarA(EstadoIncidencia destino) {
        return switch (this) {
            case PENDIENTE -> destino == REVISION;
            case REVISION -> destino == RESUELTA;
            case RESUELTA -> false;
        };
    }
}
