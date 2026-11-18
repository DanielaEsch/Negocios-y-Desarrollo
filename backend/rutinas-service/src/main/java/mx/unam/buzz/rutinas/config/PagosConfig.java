package mx.unam.buzz.rutinas.config;

import mx.unam.buzz.rutinas.integration.pasarela.PagosProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registra la configuracion de cobro (taskin.pagos).
 */
@Configuration
@EnableConfigurationProperties(PagosProperties.class)
public class PagosConfig {
}
