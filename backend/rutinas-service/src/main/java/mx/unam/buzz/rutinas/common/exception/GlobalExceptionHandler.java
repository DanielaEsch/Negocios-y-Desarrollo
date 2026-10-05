package mx.unam.buzz.rutinas.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.unam.buzz.rutinas.common.constant.CommonErrorCode;
import mx.unam.buzz.rutinas.common.vo.ErrorResponseVO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Convierte cualquier excepcion en un ErrorResponseVO con el mismo formato.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseVO> handleBusiness(BusinessException ex) {
        return build(ex.getStatus(), ex.getCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseVO> handleBadBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, CommonErrorCode.INVALID_REQUEST.getCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseVO> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, CommonErrorCode.UNKNOWN_ERROR.getCode());
    }

    private ResponseEntity<ErrorResponseVO> build(HttpStatus status, Integer code) {
        String message = messageSource.getMessage("error." + code, null,
                "Error " + code, LocaleContextHolder.getLocale());
        return ResponseEntity.status(status)
                .body(new ErrorResponseVO(Boolean.FALSE, code, message, LocalDateTime.now()));
    }
}
