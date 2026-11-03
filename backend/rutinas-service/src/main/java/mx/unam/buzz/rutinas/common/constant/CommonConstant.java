package mx.unam.buzz.rutinas.common.constant;

/**
 * Constantes que comparten todos los modulos.
 */
public final class CommonConstant {

    /** Tope del parametro size en los listados paginados. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Prefijo del campo type de las respuestas de error (RFC 7807). */
    public static final String PROBLEM_TYPE_PREFIX = "urn:buzz-rutinas:problema:";

    private CommonConstant() {
    }
}
