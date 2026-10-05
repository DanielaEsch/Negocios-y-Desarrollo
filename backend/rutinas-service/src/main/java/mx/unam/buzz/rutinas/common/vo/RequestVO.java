package mx.unam.buzz.rutinas.common.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Peticion uniforme hacia la fachada y el servicio.
 * Los datos de negocio viajan en parameters; la paginacion es opcional.
 */
@Getter
@Setter
@NoArgsConstructor
public class RequestVO<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private T parameters;
    private int page;
    private int size = 50;
    private String orderBy = "id";
    private String orderType = "ASC";

    public RequestVO(T parameters) {
        this.parameters = parameters;
    }
}
