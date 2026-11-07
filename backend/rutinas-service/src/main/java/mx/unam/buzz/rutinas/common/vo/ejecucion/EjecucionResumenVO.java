package mx.unam.buzz.rutinas.common.vo.ejecucion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Una ronda en el listado: que rutina, a que hora, quien la tomo y cuanto lleva.
 */
@Getter
@Setter
@NoArgsConstructor
public class EjecucionResumenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "41")
    private Long id;

    @Schema(example = "1")
    private Long idRutina;

    @Schema(example = "REVISION DE BANOS")
    private String rutina;

    @Schema(example = "3")
    private Long idArea;

    @Schema(example = "MANTENIMIENTO")
    private String area;

    @Schema(example = "CADA_2_HORAS")
    private String frecuencia;

    @Schema(example = "2026-11-05T14:00:00")
    private LocalDateTime fechaProgramada;

    @Schema(description = "PROGRAMADA, EN_CURSO, COMPLETADA, CANCELADA o VENCIDA", example = "PROGRAMADA")
    private String estado;

    @Schema(example = "3")
    private Long idOperador;

    @Schema(example = "Ana Torres")
    private String operador;

    @Schema(example = "4")
    private int tareasTotal;

    @Schema(example = "1")
    private int tareasCompletadas;

    @Schema(description = "Incidencias pendientes o en revision", example = "0")
    private int incidenciasAbiertas;

    @Schema(description = "Version para el bloqueo optimista; se manda de vuelta en cada PATCH", example = "0")
    private Long version;
}
