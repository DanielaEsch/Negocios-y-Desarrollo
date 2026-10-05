package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error del modulo de areas (1000-1099).
 */
@Getter
@RequiredArgsConstructor
public enum AreaErrorCode {
    NAME_REQUIRED(1001),
    ALREADY_EXISTS(1002),
    NOT_FOUND(1003);

    private final Integer code;
}
