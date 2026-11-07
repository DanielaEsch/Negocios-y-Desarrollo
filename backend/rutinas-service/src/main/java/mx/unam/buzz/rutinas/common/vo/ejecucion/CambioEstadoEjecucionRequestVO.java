package mx.unam.buzz.rutinas.common.vo.ejecucion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Cambio de estado de una ronda o de una de sus tareas. version es la que el cliente leyo.
 */
@Getter
@Setter
@NoArgsConstructor
public class CambioEstadoEjecucionRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(hidden = true)
    private Long idTarea;

    @Schema(description = "Ronda: EN_CURSO, COMPLETADA o CANCELADA. Tarea: EN_PROCESO o COMPLETADA",
            example = "EN_CURSO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.estado.obligatorio}")
    private String estado;

    @Schema(example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validacion.version.obligatoria}")
    private Long version;
}
