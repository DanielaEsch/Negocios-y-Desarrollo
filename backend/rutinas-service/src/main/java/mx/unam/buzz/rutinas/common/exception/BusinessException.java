package mx.unam.buzz.rutinas.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Error de negocio. El mensaje se resuelve con el codigo en messages.properties.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final HttpStatus status;

    public BusinessException(Integer code, HttpStatus status) {
        super("error." + code);
        this.code = code;
        this.status = status;
    }
}
