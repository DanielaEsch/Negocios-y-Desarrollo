package mx.unam.buzz.rutinas.common.vo.rutina;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Filtros opcionales del listado de rutinas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindListRutinaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long idArea;
    private String nombre;
}
