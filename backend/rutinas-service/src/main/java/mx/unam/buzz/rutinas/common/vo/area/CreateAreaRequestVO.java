package mx.unam.buzz.rutinas.common.vo.area;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.constant.AreaConstant;

import java.io.Serial;
import java.io.Serializable;

/**
 * Entrada para crear un area. Las reglas se validan antes de llegar a la fachada.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAreaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Nombre del area; se guarda en mayusculas",
            requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, example = "Cocina")
    @NotBlank(message = "{validacion.nombre.obligatorio}")
    @Size(max = AreaConstant.MAX_SIZE_NOMBRE, message = "{validacion.nombre.largo}")
    private String nombre;

    @Schema(description = "Descripcion opcional", example = "Preparacion de alimentos")
    @Size(max = AreaConstant.MAX_SIZE_DESCRIPCION, message = "{validacion.descripcion.largo}")
    private String descripcion;
}
