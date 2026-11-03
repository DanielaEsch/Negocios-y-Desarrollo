package mx.unam.buzz.rutinas.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Respuesta con un solo objeto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SingleResponseVO<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "true cuando la operacion se completo", example = "true")
    private Boolean success;

    @Schema(description = "Resultado de la operacion")
    private T data;

    public static <T> SingleResponseVO<T> ok(T data) {
        return new SingleResponseVO<>(Boolean.TRUE, data);
    }
}
