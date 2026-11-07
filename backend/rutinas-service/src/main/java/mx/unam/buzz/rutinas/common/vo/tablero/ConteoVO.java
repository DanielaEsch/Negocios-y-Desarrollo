package mx.unam.buzz.rutinas.common.vo.tablero;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Conteo de rondas de un dia o de un area.
 */
@Getter
@Setter
@NoArgsConstructor
public class ConteoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "COCINA")
    private String nombre;

    @Schema(example = "2026-11-05")
    private LocalDate fecha;

    private int total;
    private int completadas;
    private int enCurso;
    private int programadas;
    private int vencidas;
    private int canceladas;

    @Schema(description = "Completadas entre las que ya se decidieron (completadas + vencidas), de 0 a 100; "
            + "nulo si todavia no hay ninguna", example = "87.5")
    private Double cumplimiento;
}
