package mx.unam.buzz.rutinas.common.constant;

import java.util.Set;

/**
 * Constantes del modulo de areas. Los largos coinciden con el DDL.
 */
public final class AreaConstant {

    public static final int MAX_SIZE_NOMBRE = 100;
    public static final int MAX_SIZE_DESCRIPCION = 250;

    /** Campos por los que se permite ordenar el listado. */
    public static final Set<String> ORDER_FIELDS = Set.of("id", "nombre", "fechaCreacion");

    private AreaConstant() {
    }
}
