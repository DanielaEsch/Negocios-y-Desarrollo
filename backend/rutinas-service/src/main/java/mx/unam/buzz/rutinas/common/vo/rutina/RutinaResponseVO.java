package mx.unam.buzz.rutinas.common.vo.rutina;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * Salida de una rutina con sus tareas en orden.
 */
@Getter
@Setter
@NoArgsConstructor
public class RutinaResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "1")
    private Long id;

    @Schema(example = "3")
    private Long idArea;

    @Schema(example = "MANTENIMIENTO")
    private String area;

    @Schema(example = "REVISION DE BANOS")
    private String nombre;

    @Schema(example = "BANOS DE CLIENTES Y DE PERSONAL")
    private String descripcion;

    @Schema(example = "CADA_2_HORAS")
    private String frecuencia;

    @Schema(type = "string", example = "08:00")
    private LocalTime horaInicio;

    @Schema(type = "string", example = "20:00")
    private LocalTime horaFin;

    @Schema(description = "1 = lunes ... 7 = domingo; solo SEMANAL", example = "1")
    private Integer diaSemana;

    private List<TareaVO> tareas;
}
