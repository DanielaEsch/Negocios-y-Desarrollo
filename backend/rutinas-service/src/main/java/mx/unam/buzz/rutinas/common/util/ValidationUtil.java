package mx.unam.buzz.rutinas.common.util;

import mx.unam.buzz.rutinas.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

/**
 * Validaciones de una linea para los servicios.
 * Uso: ValidationUtil.validate(condicionDeError, codigo, HttpStatus.BAD_REQUEST);
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static void validate(boolean error, Integer code, HttpStatus status) {
        if (error) {
            throw new BusinessException(code, status);
        }
    }

    public static boolean isNullOrZero(Long value) {
        return value == null || value == 0L;
    }

    /** Recorta espacios, limita el largo y pasa a mayusculas. */
    public static String normalize(String value, int maxLength) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return StringUtils.left(value.trim(), maxLength).toUpperCase();
    }
}
