package mx.unam.buzz.rutinas.web;

/**
 * Ejemplos de error que Swagger muestra en el modulo de areas. Son respuestas reales del
 * GlobalExceptionHandler; si cambia el formato de los errores, hay que copiarlas de nuevo.
 */
final class AreaApiExamples {

    static final String VALIDACION = """
            {"type": "urn:buzz-rutinas:problema:validacion",
             "title": "Error de Validación en los Datos de Entrada", "status": 400,
             "detail": "Uno o más campos del payload no cumplen con las reglas establecidas.",
             "instance": "/api/v1/areas", "timestamp": "2026-10-27T13:35:41.004708",
             "invalid_params": [{"name": "nombre", "reason": "El nombre es obligatorio."}]}""";

    static final String CAMPO_DESCONOCIDO = """
            {"type": "urn:buzz-rutinas:problema:peticion-invalida", "title": "Bad Request", "status": 400,
             "detail": "La petición no es válida.", "instance": "/api/v1/areas",
             "timestamp": "2026-10-27T13:35:41.079319", "code": 1}""";

    static final String ID_INVALIDO = """
            {"type": "about:blank", "title": "Bad Request", "status": 400,
             "detail": "Failed to convert 'id' with value: 'abc'", "instance": "/api/v1/areas/abc",
             "timestamp": "2026-10-27T13:36:44.722923"}""";

    static final String PAGINA = """
            {"type": "urn:buzz-rutinas:problema:validacion",
             "title": "Error de Validación en los Datos de Entrada", "status": 400,
             "detail": "Uno o más campos del payload no cumplen con las reglas establecidas.",
             "instance": "/api/v1/areas", "timestamp": "2026-10-27T13:36:44.711535",
             "invalid_params": [{"name": "size", "reason": "El tamaño de página admite como máximo 100."}]}""";

    static final String NO_EXISTE = """
            {"type": "urn:buzz-rutinas:problema:negocio", "title": "Not Found", "status": 404,
             "detail": "El área no existe o fue eliminada.", "instance": "/api/v1/areas/99",
             "timestamp": "2026-10-27T13:39:29.608691", "code": 1003}""";

    static final String DUPLICADO = """
            {"type": "urn:buzz-rutinas:problema:negocio", "title": "Conflict", "status": 409,
             "detail": "Ya existe un área activa con ese nombre.", "instance": "/api/v1/areas",
             "timestamp": "2026-10-27T13:39:29.565921", "code": 1002}""";

    static final String DUPLICADO_AL_ACTUALIZAR = """
            {"type": "urn:buzz-rutinas:problema:negocio", "title": "Conflict", "status": 409,
             "detail": "Ya existe un área activa con ese nombre.", "instance": "/api/v1/areas/2",
             "timestamp": "2026-10-27T13:39:29.598192", "code": 1002}""";

    private AreaApiExamples() {
    }
}
