package mx.unam.buzz.rutinas.common.vo.ejecucion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Filtros del listado de rondas. Sin fecha se toma hoy.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindListEjecucionRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate fecha;
    private String estado;
    private Long idArea;
    private boolean soloMias;
}
