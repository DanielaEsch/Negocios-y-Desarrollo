package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error del modulo de incidencias (1500-1599).
 */
@Getter
@RequiredArgsConstructor
public enum IncidenciaErrorCode {
    NOT_FOUND(1501),
    STATE_NOT_SUPPORTED(1502),
    AREA_FORBIDDEN(1503);

    private final Integer code;
}
