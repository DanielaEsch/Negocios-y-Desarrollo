package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error generales (0-999). Cada modulo usa su propio rango.
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode {
    UNKNOWN_ERROR(0),
    INVALID_REQUEST(1),
    PARAMETERS_REQUIRED(2),
    ID_REQUIRED(3),
    NO_RESULTS(404);

    private final Integer code;
}
