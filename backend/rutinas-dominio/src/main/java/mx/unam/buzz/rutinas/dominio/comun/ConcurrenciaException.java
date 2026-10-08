package mx.unam.buzz.rutinas.dominio.comun;

/**
 * Otro usuario modifico la entidad despues de que este la leyo (bloqueo
 * optimista). No es un error de negocio: quien llama debe recargar y decidir si
 * reintenta.
 */
public class ConcurrenciaException extends RuntimeException {

    public ConcurrenciaException(String mensaje) {
        super(mensaje);
    }
}
