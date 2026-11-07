package mx.unam.buzz.rutinas.common.vo.ejecucion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Avance de una tarea dentro de la ronda.
 */
@Getter
@Setter
@NoArgsConstructor
public class TareaEjecucionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "12")
    private Long idTarea;

    @Schema(example = "Revisar papel, jabon y toallas")
    private String nombre;

    @Schema(example = "1")
    private Integer orden;

    @Schema(description = "PENDIENTE, EN_PROCESO o COMPLETADA", example = "PENDIENTE")
    private String estado;

    private LocalDateTime fechaCompletada;
}
