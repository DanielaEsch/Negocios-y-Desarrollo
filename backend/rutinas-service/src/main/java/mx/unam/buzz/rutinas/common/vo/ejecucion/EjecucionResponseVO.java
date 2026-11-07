package mx.unam.buzz.rutinas.common.vo.ejecucion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle de una ronda: el resumen mas sus tareas e incidencias.
 */
@Getter
@Setter
@NoArgsConstructor
public class EjecucionResponseVO extends EjecucionResumenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "BANOS DE CLIENTES Y DE PERSONAL")
    private String descripcionRutina;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private List<TareaEjecucionVO> tareas;

    private List<IncidenciaResponseVO> incidencias;
}
