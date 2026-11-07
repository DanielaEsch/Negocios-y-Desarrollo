package mx.unam.buzz.rutinas.common.constant;

import java.util.Set;

/**
 * Constantes del modulo de usuarios. Los largos coinciden con el DDL.
 */
public final class UsuarioConstant {

    public static final int MAX_SIZE_NOMBRE = 150;
    public static final int MAX_SIZE_CORREO = 150;
    public static final int MAX_SIZE_CONTRASENA = 72;

    public static final Set<String> ORDER_FIELDS = Set.of("id", "nombre", "correo", "perfil");

    private UsuarioConstant() {
    }
}
