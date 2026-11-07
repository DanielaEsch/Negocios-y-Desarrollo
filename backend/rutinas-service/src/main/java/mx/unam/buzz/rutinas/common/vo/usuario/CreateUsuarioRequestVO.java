package mx.unam.buzz.rutinas.common.vo.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.constant.UsuarioConstant;
import mx.unam.buzz.rutinas.dominio.usuario.Usuario;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Alta de un usuario. Entra como OPERADOR; la promocion es otra operacion.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateUsuarioRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "Carla Ruiz", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.nombre.obligatorio}")
    @Size(max = UsuarioConstant.MAX_SIZE_NOMBRE, message = "{validacion.nombre.largo}")
    private String nombre;

    @Schema(example = "carla@taskin.mx", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.correo.obligatorio}")
    @Email(message = "{validacion.correo.formato}")
    @Size(max = UsuarioConstant.MAX_SIZE_CORREO, message = "{validacion.correo.largo}")
    private String correo;

    @Schema(description = "Minimo 8 caracteres", example = "TaskIn2026", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.contrasena.obligatoria}")
    @Size(min = Usuario.LONGITUD_MINIMA_CONTRASENA, max = UsuarioConstant.MAX_SIZE_CONTRASENA,
            message = "{validacion.contrasena.largo}")
    private String contrasena;

    @Schema(description = "Acepto el aviso de privacidad (LFPDPPP)", example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @AssertTrue(message = "{validacion.consentimiento.obligatorio}")
    private boolean aceptaAvisoPrivacidad;

    @Schema(description = "Ids de las areas a su cargo", example = "[3]")
    @NotEmpty(message = "{validacion.areas.obligatorias}")
    private List<Long> areas;
}
