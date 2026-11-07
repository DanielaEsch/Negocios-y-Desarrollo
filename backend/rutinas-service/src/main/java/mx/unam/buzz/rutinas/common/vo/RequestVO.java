package mx.unam.buzz.rutinas.common.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;

import java.io.Serial;
import java.io.Serializable;

/**
 * Peticion uniforme hacia la fachada y el servicio.
 * Los datos de negocio viajan en parameters; la paginacion es opcional y sesion es
 * quien pide (la llena la fachada).
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
    private UsuarioSesionVO sesion;

    public RequestVO(T parameters) {
        this.parameters = parameters;
    }
}
