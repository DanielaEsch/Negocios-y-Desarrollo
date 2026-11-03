package mx.unam.buzz.rutinas.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.unam.buzz.rutinas.common.constant.CommonConstant;
import mx.unam.buzz.rutinas.common.constant.CommonErrorCode;
import mx.unam.buzz.rutinas.dominio.comun.ConcurrenciaException;
import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.comun.TransicionInvalidaException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Convierte cualquier excepcion en un ProblemDetail (RFC 7807, hoy RFC 9457) con
 * Content-Type application/problem+json. Los errores propios de Spring MVC
 * (404 de ruta, 405, tipos invalidos) los resuelve ResponseEntityExceptionHandler
 * con el mismo formato.
 *
 * Campos: type, title, status, detail, instance y, como extensiones, timestamp,
 * code (codigo de messages.properties) e invalid_params (un elemento por campo
 * invalido, con name y reason, como el ejemplo del RFC).
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(ex.getStatus(), "negocio", ex.getCode(), request.getRequestURI());
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    public ProblemDetail handleTransicion(TransicionInvalidaException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "transicion-invalida", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConcurrenciaException.class)
    public ProblemDetail handleConcurrencia(ConcurrenciaException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "concurrencia", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail handleRegla(ReglaNegocioException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "regla-negocio", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno",
                CommonErrorCode.UNKNOWN_ERROR.getCode(), request.getRequestURI());
    }

    /** Cuerpo con @Valid que no cumple las restricciones del DTO. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<InvalidParam> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> new InvalidParam(error instanceof FieldError field ? field.getField()
                        : error.getObjectName(), error.getDefaultMessage()))
                .toList();
        return handleExceptionInternal(ex, validation(errors, path(request)), headers, status, request);
    }

    /** Parametros de ruta o de query con restricciones (@Min, @Max...) que no se cumplen. */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<InvalidParam> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new InvalidParam(error instanceof FieldError field ? field.getField()
                                : result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return handleExceptionInternal(ex, validation(errors, path(request)), headers, status, request);
    }

    /** JSON mal formado, tipos incorrectos o campos que el DTO no declara. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = build(HttpStatus.BAD_REQUEST, "peticion-invalida",
                CommonErrorCode.INVALID_REQUEST.getCode(), path(request));
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /** Completa instance y timestamp en los errores que arma Spring MVC. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            complete(problem, path(request));
        }
        return response;
    }

    private ProblemDetail validation(List<InvalidParam> invalidParams, String path) {
        ProblemDetail problem = build(HttpStatus.BAD_REQUEST, "validacion",
                "Uno o más campos del payload no cumplen con las reglas establecidas.", path);
        problem.setTitle("Error de Validación en los Datos de Entrada");
        problem.setProperty("invalid_params", invalidParams);
        return problem;
    }

    private ProblemDetail build(HttpStatusCode status, String type, Integer code, String path) {
        String message = messageSource.getMessage("error." + code, null,
                "Error " + code, LocaleContextHolder.getLocale());
        ProblemDetail problem = build(status, type, message, path);
        problem.setProperty("code", code);
        return problem;
    }

    private ProblemDetail build(HttpStatusCode status, String type, String detail, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(CommonConstant.PROBLEM_TYPE_PREFIX + type));
        complete(problem, path);
        return problem;
    }

    private void complete(ProblemDetail problem, String path) {
        if (problem.getInstance() == null && path != null) {
            problem.setInstance(URI.create(path));
        }
        if (problem.getProperties() == null || !problem.getProperties().containsKey("timestamp")) {
            problem.setProperty("timestamp", LocalDateTime.now());
        }
    }

    /** Un campo que no cumple su regla: name es el campo y reason, el mensaje de messages.properties. */
    public record InvalidParam(String name, String reason) {
    }

    private static String path(WebRequest request) {
        return request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : null;
    }
}
