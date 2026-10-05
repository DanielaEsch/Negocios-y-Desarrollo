package mx.unam.buzz.rutinas.config;

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

    /** Usuario del sistema mientras no exista autenticacion. */
    public static final Long USUARIO_SISTEMA = 0L;

    @Bean
    public AuditorAware<Long> auditorAware() {
        // TODO: tomar el id del usuario autenticado cuando entre el modulo de seguridad
        return () -> Optional.of(USUARIO_SISTEMA);
    }
}
