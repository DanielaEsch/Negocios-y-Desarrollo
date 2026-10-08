package mx.unam.buzz.rutinas.dominio.comun;

/**
 * Se intento mover una entidad a un estado que su maquina de estados no permite
 * desde el estado actual (por ejemplo, saltarse un paso).
 */
public class TransicionInvalidaException extends ReglaNegocioException {

    public TransicionInvalidaException(String entidad, Enum<?> origen, Enum<?> destino) {
        super(entidad + ": no se puede pasar de " + origen + " a " + destino);
    }
}
