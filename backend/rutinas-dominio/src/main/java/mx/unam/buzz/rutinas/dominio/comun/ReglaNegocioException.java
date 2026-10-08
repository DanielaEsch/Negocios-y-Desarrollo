package mx.unam.buzz.rutinas.dominio.comun;

/**
 * Una regla del dominio no se cumple. No sabe nada de HTTP: la capa de servicio
 * decide como responder.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
