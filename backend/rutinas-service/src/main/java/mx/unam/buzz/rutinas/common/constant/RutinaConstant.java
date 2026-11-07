package mx.unam.buzz.rutinas.common.constant;

import java.util.Set;

/**
 * Constantes del modulo de rutinas. Los largos coinciden con el DDL.
 */
public final class RutinaConstant {

    public static final int MAX_SIZE_NOMBRE = 100;
    public static final int MAX_SIZE_DESCRIPCION = 250;
    public static final int MAX_SIZE_TAREA = 150;
    public static final int MAX_TAREAS = 30;

    public static final Set<String> ORDER_FIELDS = Set.of("id", "nombre", "frecuencia", "horaInicio");

    private RutinaConstant() {
    }
}
