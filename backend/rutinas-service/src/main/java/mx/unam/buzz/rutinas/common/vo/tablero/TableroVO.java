package mx.unam.buzz.rutinas.common.vo.tablero;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen del dia para el supervisor: cumplimiento, por area, tendencia e incidencias abiertas.
 */
@Getter
@Setter
@NoArgsConstructor
public class TableroVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "2026-11-05")
    private LocalDate fecha;

    @Schema(description = "Totales del dia")
    private ConteoVO dia;

    @Schema(description = "Un conteo por area")
    private List<ConteoVO> porArea;

    @Schema(description = "Un conteo por dia, del mas antiguo a hoy")
    private List<ConteoVO> tendencia;

    @Schema(example = "3")
    private long incidenciasAbiertas;
}
