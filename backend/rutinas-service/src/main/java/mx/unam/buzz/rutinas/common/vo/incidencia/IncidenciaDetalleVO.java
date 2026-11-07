package mx.unam.buzz.rutinas.common.vo.incidencia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Incidencia vista por un supervisor: agrega quien la reporto y quien la revisa.
 */
@Getter
@Setter
@NoArgsConstructor
public class IncidenciaDetalleVO extends IncidenciaResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "3")
    private Long idReporta;

    @Schema(example = "Ana Torres")
    private String reporta;

    @Schema(example = "2")
    private Long idRevisor;

    @Schema(example = "Jorge Ramirez")
    private String revisor;
}
