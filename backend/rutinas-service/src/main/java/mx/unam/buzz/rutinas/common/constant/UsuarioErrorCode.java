package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error del modulo de usuarios (1200-1299).
 */
@Getter
@RequiredArgsConstructor
public enum UsuarioErrorCode {
    EMAIL_ALREADY_EXISTS(1201),
    NOT_FOUND(1202),
    AREA_NOT_FOUND(1203),
    PROFILE_NOT_SUPPORTED(1204),
    SELF_DELETE(1205);

    private final Integer code;
}
