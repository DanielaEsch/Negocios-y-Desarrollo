package mx.unam.buzz.rutinas.common.vo.area;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Salida de un area. Es lo que viaja en data de SingleResponseVO y ResponseVO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AreaResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Id del area", example = "1")
    private Long id;

    @Schema(description = "Nombre del area, siempre en mayusculas", example = "COCINA")
    private String nombre;

    @Schema(description = "Descripcion; puede venir nula", example = "Preparacion de alimentos")
    private String descripcion;

    @Schema(description = "Fecha y hora del alta", example = "2026-10-27T13:35:41.004708")
    private LocalDateTime fechaCreacion;
}
