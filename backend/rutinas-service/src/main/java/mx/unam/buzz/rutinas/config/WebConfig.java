package mx.unam.buzz.rutinas.config;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.security.AutenticacionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra la autenticacion por token en todas las rutas; el interceptor decide
 * cuales son endpoints propios.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacionInterceptor autenticacionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacionInterceptor);
    }
}
