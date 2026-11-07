package mx.unam.buzz.rutinas.common.vo.rutina;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.constant.RutinaConstant;
import mx.unam.buzz.rutinas.dominio.rutina.Frecuencia;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * Alta o cambio de una rutina. Las tareas se mandan en orden y reemplazan a las anteriores.
 */
@Getter
@Setter
@NoArgsConstructor
public class SaveRutinaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validacion.area.obligatoria}")
    private Long idArea;

    @Schema(example = "Revision de banos", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.nombre.obligatorio}")
    @Size(max = RutinaConstant.MAX_SIZE_NOMBRE, message = "{validacion.nombre.largo}")
    private String nombre;

    @Schema(example = "Banos de clientes y de personal")
    @Size(max = RutinaConstant.MAX_SIZE_DESCRIPCION, message = "{validacion.descripcion.largo}")
    private String descripcion;

    @Schema(example = "CADA_2_HORAS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validacion.frecuencia.obligatoria}")
    private Frecuencia frecuencia;

    @Schema(type = "string", example = "08:00")
    private LocalTime horaInicio;

    @Schema(type = "string", example = "20:00")
    private LocalTime horaFin;

    @Schema(description = "1 = lunes ... 7 = domingo; solo SEMANAL", example = "1")
    @Min(value = 1, message = "{validacion.dia.rango}")
    @Max(value = 7, message = "{validacion.dia.rango}")
    private Integer diaSemana;

    @Schema(description = "Tareas en el orden en que se hacen",
            example = "[\"Revisar papel y jabon\", \"Trapear piso\"]")
    @NotEmpty(message = "{validacion.tareas.obligatorias}")
    @Size(max = RutinaConstant.MAX_TAREAS, message = "{validacion.tareas.maximo}")
    private List<@NotBlank(message = "{validacion.tarea.obligatoria}")
            @Size(max = RutinaConstant.MAX_SIZE_TAREA, message = "{validacion.tarea.largo}") String> tareas;
}
