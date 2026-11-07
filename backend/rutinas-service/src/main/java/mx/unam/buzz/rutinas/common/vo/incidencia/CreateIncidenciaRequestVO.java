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
 * Reporte de una incidencia durante una ronda.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateIncidenciaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long idEjecucion;

    @Schema(example = "Se acabo el jabon del bano 2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.descripcion.obligatoria}")
    @Size(max = IncidenciaConstant.MAX_SIZE_DESCRIPCION, message = "{validacion.incidencia.largo}")
    private String descripcion;
}
