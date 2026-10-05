package mx.unam.buzz.rutinas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion de la API en /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rutinasOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Control de Rutinas API")
                .version("0.0.1")
                .description("API del sistema de control de rutinas e incidencias - Equipo Buzz"));
    }
}
