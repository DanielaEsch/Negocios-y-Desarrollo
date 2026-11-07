package mx.unam.buzz.rutinas.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * Quien hace la peticion. Lo arma el interceptor de autenticacion a partir del
 * token y viaja hacia el servicio dentro de RequestVO.
 */
@Getter
@AllArgsConstructor
public class UsuarioSesionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nombre;
    private final String correo;
    private final Perfil perfil;
    private final Set<Long> areas;

    public boolean esAdministrador() {
        return perfil == Perfil.ADMINISTRADOR;
    }

    /** SUPERVISOR o ADMINISTRADOR: los perfiles que revisan. */
    public boolean puedeSupervisar() {
        return perfil == Perfil.SUPERVISOR || perfil == Perfil.ADMINISTRADOR;
    }

    /** El administrador ve todas las areas; los demas, solo las asignadas. */
    public boolean tieneArea(Long idArea) {
        return esAdministrador() || areas.contains(idArea);
    }
}
