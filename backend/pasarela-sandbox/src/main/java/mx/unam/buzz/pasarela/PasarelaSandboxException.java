package mx.unam.buzz.pasarela;

import org.springframework.http.HttpStatus;

/**
 * Error del simulador con el formato propio de la pasarela ({@code {"error": {...}}}),
 * distinto del ProblemDetail de TaskIn: son dos sistemas diferentes.
 */
public class PasarelaSandboxException extends RuntimeException {

    private final HttpStatus status;
    private final String tipo;
    private final String codigo;

    public PasarelaSandboxException(HttpStatus status, String tipo, String codigo, String mensaje) {
        super(mensaje);
        this.status = status;
        this.tipo = tipo;
        this.codigo = codigo;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    static PasarelaSandboxException solicitud(String codigo, String mensaje) {
        return new PasarelaSandboxException(HttpStatus.BAD_REQUEST, "solicitud_invalida", codigo, mensaje);
    }
}
