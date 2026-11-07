package mx.unam.buzz.rutinas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion de la API en /api/v1/api-docs (Swagger UI). Para probar desde ahi:
 * POST /auth/login, copiar el token y pegarlo en "Authorize".
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearer";

    @Bean
    public OpenAPI rutinasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskIn API")
                        .version("0.1.0")
                        .description("API del sistema de control de rutinas e incidencias - Equipo Buzz"))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
