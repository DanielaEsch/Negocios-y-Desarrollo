package mx.unam.buzz.rutinas.common.vo.incidencia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Incidencia vista por quien la reporto: que paso, en que va y como se resolvio.
 */
@Getter
@Setter
@NoArgsConstructor
public class IncidenciaResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "5")
    private Long id;

    @Schema(example = "41")
    private Long idEjecucion;

    @Schema(example = "REVISION DE BANOS")
    private String rutina;

    @Schema(example = "3")
    private Long idArea;

    @Schema(example = "MANTENIMIENTO")
    private String area;

    @Schema(example = "Se acabo el jabon del bano 2")
    private String descripcion;

    @Schema(description = "PENDIENTE, REVISION o RESUELTA", example = "PENDIENTE")
    private String estado;

    private LocalDateTime fechaReporte;

    @Schema(example = "Se repuso el jabon y se pidio mas al proveedor")
    private String comentarioResolucion;

    private LocalDateTime fechaResolucion;
}
