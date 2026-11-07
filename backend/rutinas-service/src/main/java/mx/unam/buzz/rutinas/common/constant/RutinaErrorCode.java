package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error del modulo de rutinas (1300-1399).
 */
@Getter
@RequiredArgsConstructor
public enum RutinaErrorCode {
    NOT_FOUND(1301),
    AREA_NOT_FOUND(1302),
    ALREADY_EXISTS(1303),
    AREA_FORBIDDEN(1304),
    NOT_BY_EVENT(1305);

    private final Integer code;
}
