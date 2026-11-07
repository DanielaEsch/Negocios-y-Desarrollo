package mx.unam.buzz.rutinas.common.vo.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Salida de un usuario. Sin hash de contrasena ni auditoria.
 */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "3")
    private Long id;

    @Schema(example = "Ana Torres")
    private String nombre;

    @Schema(example = "ana@taskin.mx")
    private String correo;

    @Schema(description = "OPERADOR, SUPERVISOR o ADMINISTRADOR", example = "OPERADOR")
    private String perfil;

    @Schema(description = "Areas a su cargo")
    private List<AreaResumenVO> areas;
}
