package mx.unam.buzz.pasarela;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Errores del simulador con su propio formato. Solo aplica a su controlador.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = PasarelaSandboxController.class)
public class PasarelaSandboxErrores {

    @ExceptionHandler(PasarelaSandboxException.class)
    public ResponseEntity<Map<String, Object>> manejar(PasarelaSandboxException ex) {
        return ResponseEntity.status(ex.getStatus()).body(cuerpo(ex.getTipo(), ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> ilegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(cuerpo("solicitud_invalida", "json_invalido", "El cuerpo no es un JSON valido"));
    }

    private static Map<String, Object> cuerpo(String tipo, String codigo, String mensaje) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("tipo", tipo);
        error.put("codigo", codigo);
        error.put("mensaje", mensaje);
        return Map.of("error", error);
    }
}
