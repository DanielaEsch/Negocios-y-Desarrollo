package mx.unam.buzz.rutinas.common.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Credenciales del login. La contrasena viaja una sola vez y nunca se guarda en claro.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Correo del usuario", example = "ana@taskin.mx", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.correo.obligatorio}")
    @Email(message = "{validacion.correo.formato}")
    private String correo;

    @Schema(description = "Contrasena", example = "TaskIn2026", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.contrasena.obligatoria}")
    private String contrasena;
}
