package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error de sesion y permisos (1100-1199).
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {
    CREDENTIALS_INVALID(1101),
    TOKEN_REQUIRED(1102),
    TOKEN_INVALID(1103),
    FORBIDDEN(1104);

    private final Integer code;
}
