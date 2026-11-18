package mx.unam.buzz.rutinas.integration.pasarela;

/**
 * La pasarela no abrio el cargo.
 * rechazo=true: la pasarela respondio con un error de tarjeta o de datos (4xx).
 * rechazo=false: no respondio o fallo de su lado; no sabemos si hubo cargo.
 */
public class PasarelaException extends RuntimeException {

    private final boolean rechazo;
    private final String codigo;

    public PasarelaException(boolean rechazo, String codigo, String mensaje) {
        super(mensaje);
        this.rechazo = rechazo;
        this.codigo = codigo;
    }

    public boolean isRechazo() {
        return rechazo;
    }

    public String getCodigo() {
        return codigo;
    }
}
