package mx.unam.buzz.rutinas.config;

import mx.unam.buzz.rutinas.common.security.SesionContexto;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Llena usuario y fecha de creacion/modificacion en AuditableDO.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /** Usuario del sistema: lo que no hace una persona (programacion, vencimientos). */
    public static final Long USUARIO_SISTEMA = 0L;

    @Bean
    public AuditorAware<Long> auditorAware() {
        // Fuera de una peticion (tareas programadas, datos de demostracion) audita el sistema
        return () -> Optional.of(SesionContexto.actual().map(UsuarioSesionVO::getId).orElse(USUARIO_SISTEMA));
    }
}
