package mx.unam.buzz.rutinas.common.vo.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Area en una sola linea: id y nombre.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AreaResumenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "3")
    private Long id;

    @Schema(example = "MANTENIMIENTO")
    private String nombre;
}
