package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error del modulo de ejecuciones (1400-1499).
 */
@Getter
@RequiredArgsConstructor
public enum EjecucionErrorCode {
    NOT_FOUND(1401),
    AREA_FORBIDDEN(1402),
    TASK_NOT_FOUND(1403),
    STATE_NOT_SUPPORTED(1404),
    VERSION_CONFLICT(1405);

    private final Integer code;
}
