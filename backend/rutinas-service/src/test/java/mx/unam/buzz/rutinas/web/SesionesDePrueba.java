package mx.unam.buzz.rutinas.web;

import mx.unam.buzz.rutinas.common.security.TokenService;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioAreaDO;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioDO;
import mx.unam.buzz.rutinas.persistence.IUsuarioAreaRepository;
import mx.unam.buzz.rutinas.persistence.IUsuarioRepository;
import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Crea usuarios en H2 y les emite token, para que las pruebas pasen por el mismo
 * interceptor de autenticacion que la API real.
 */
@TestComponent
public class SesionesDePrueba {

    private final IUsuarioRepository usuarioRepository;
    private final IUsuarioAreaRepository usuarioAreaRepository;
    private final TokenService tokenService;

    public SesionesDePrueba(IUsuarioRepository usuarioRepository, IUsuarioAreaRepository usuarioAreaRepository,
                            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAreaRepository = usuarioAreaRepository;
        this.tokenService = tokenService;
    }

    /** Usuario con correo unico; el hash no importa porque el token se emite directo. */
    public Usuario crear(Perfil perfil, List<Long> areas) {
        UsuarioDO entity = new UsuarioDO();
        entity.setNombre(perfil.name() + " de prueba");
        entity.setCorreo(UUID.randomUUID() + "@prueba.mx");
        entity.setHashContrasena("sin-uso");
        entity.setPerfil(perfil.name());
        entity.setConsentimientoDatos(true);
        entity.setFechaConsentimiento(LocalDateTime.now());
        usuarioRepository.save(entity);
        areas.forEach(idArea -> {
            UsuarioAreaDO asignacion = new UsuarioAreaDO();
            asignacion.setIdUsuario(entity.getId());
            asignacion.setIdArea(idArea);
            usuarioAreaRepository.save(asignacion);
        });
        return new Usuario(entity.getId(), "Bearer " + tokenService.emitir(entity.getId(), perfil.name()).valor());
    }

    public record Usuario(Long id, String bearer) {
    }
}
