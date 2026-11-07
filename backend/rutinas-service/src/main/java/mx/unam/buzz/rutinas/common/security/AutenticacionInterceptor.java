package mx.unam.buzz.rutinas.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.AuthErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.service.IAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * Cadena de seguridad minima mientras no entra Spring Security: valida el token
 * Bearer de cada endpoint propio, deja la sesion en SesionContexto y revisa
 * @RequierePerfil. Swagger y actuator no pasan por aqui (no son controladores del
 * paquete web).
 *
 * Los errores se lanzan como BusinessException y los convierte en ProblemDetail el
 * GlobalExceptionHandler, igual que cualquier otro error de la API.
 */
@Component
@RequiredArgsConstructor
public class AutenticacionInterceptor implements HandlerInterceptor {

    private static final String PAQUETE_WEB = "mx.unam.buzz.rutinas.web";
    private static final String BEARER = "Bearer ";

    private final IAuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)
                || !method.getBeanType().getPackageName().startsWith(PAQUETE_WEB)
                || method.hasMethodAnnotation(Publico.class)) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            throw new BusinessException(AuthErrorCode.TOKEN_REQUIRED.getCode(), HttpStatus.UNAUTHORIZED);
        }
        UsuarioSesionVO sesion = authService.sesionDesdeToken(header.substring(BEARER.length()).trim());

        RequierePerfil requiere = method.getMethodAnnotation(RequierePerfil.class);
        if (requiere != null && Arrays.stream(requiere.value()).noneMatch(perfil -> perfil == sesion.getPerfil())) {
            throw new BusinessException(AuthErrorCode.FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);
        }
        SesionContexto.establecer(sesion);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        SesionContexto.limpiar();
    }
}
