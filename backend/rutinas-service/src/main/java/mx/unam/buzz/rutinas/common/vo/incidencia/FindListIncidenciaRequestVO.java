package mx.unam.buzz.rutinas.common.vo.incidencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Filtros opcionales del listado de incidencias.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindListIncidenciaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String estado;
    private Long idArea;
}
