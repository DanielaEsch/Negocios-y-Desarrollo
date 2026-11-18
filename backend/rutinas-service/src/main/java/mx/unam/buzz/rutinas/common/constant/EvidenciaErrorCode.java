package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error del modulo de evidencias (1700-1799).
 */
@Getter
@RequiredArgsConstructor
public enum EvidenciaErrorCode {
    NOT_FOUND(1701),
    ARCHIVO_OBLIGATORIO(1702),
    DEMASIADO_GRANDE(1703),
    ALMACENAMIENTO(1704);

    private final Integer code;
}
