package mx.unam.buzz.rutinas.common.vo.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Salida de un usuario para el administrador: agrega los datos de consentimiento (derechos ARCO).
 */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioAdminVO extends UsuarioResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Fecha en que acepto el aviso de privacidad")
    private LocalDateTime fechaConsentimiento;

    @Schema(description = "Fecha de alta")
    private LocalDateTime fechaCreacion;
}
