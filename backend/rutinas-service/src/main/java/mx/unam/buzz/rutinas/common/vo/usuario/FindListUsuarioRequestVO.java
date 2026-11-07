package mx.unam.buzz.rutinas.common.vo.usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Filtros opcionales del listado de usuarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindListUsuarioRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String perfil;
}
