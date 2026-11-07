package mx.unam.buzz.rutinas.common.vo.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;

import java.io.Serial;
import java.io.Serializable;

/**
 * Cambio de perfil. Hoy solo se admite la promocion a SUPERVISOR.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdatePerfilRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(example = "SUPERVISOR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validacion.perfil.obligatorio}")
    private Perfil perfil;
}
