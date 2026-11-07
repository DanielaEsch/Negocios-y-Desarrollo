package mx.unam.buzz.rutinas.common.vo.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Reemplaza las areas a cargo de un usuario.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateAreasRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(example = "[2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validacion.areas.obligatorias}")
    private List<Long> areas;
}
