package mx.unam.buzz.rutinas.common.security;

import java.util.Optional;

/**
 * Sesion de la peticion en curso. La llena AutenticacionInterceptor al entrar y la
 * limpia al terminar; la leen la fachada (para armar RequestVO) y la auditoria.
 */
public final class SesionContexto {

    private static final ThreadLocal<UsuarioSesionVO> ACTUAL = new ThreadLocal<>();

    private SesionContexto() {
    }

    public static void establecer(UsuarioSesionVO sesion) {
        ACTUAL.set(sesion);
    }

    public static Optional<UsuarioSesionVO> actual() {
        return Optional.ofNullable(ACTUAL.get());
    }

    public static void limpiar() {
        ACTUAL.remove();
    }
}
