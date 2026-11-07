package mx.unam.buzz.rutinas.common.vo.incidencia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.constant.IncidenciaConstant;

import java.io.Serial;
import java.io.Serializable;

/**
 * Cambio de estado de una incidencia. RESUELTA pide comentario.
 */
@Getter
@Setter
@NoArgsConstructor
public class CambioEstadoIncidenciaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(description = "REVISION o RESUELTA", example = "REVISION", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.estado.obligatorio}")
    private String estado;

    @Schema(description = "Obligatorio para RESUELTA", example = "Se repuso el jabon")
    @Size(max = IncidenciaConstant.MAX_SIZE_COMENTARIO, message = "{validacion.incidencia.largo}")
    private String comentarioResolucion;
}
