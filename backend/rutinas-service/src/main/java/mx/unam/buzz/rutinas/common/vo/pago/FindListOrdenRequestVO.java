package mx.unam.buzz.rutinas.common.vo.pago;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Filtros del listado de ordenes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindListOrdenRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String estado;
    private String metodo;
}
