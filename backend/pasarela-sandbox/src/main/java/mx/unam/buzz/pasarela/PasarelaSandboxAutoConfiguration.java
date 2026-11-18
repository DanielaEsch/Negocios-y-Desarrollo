package mx.unam.buzz.pasarela;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;

/**
 * Se monta solo con {@code pasarela.sandbox.habilitada=true}. TaskIn no escanea este
 * paquete: el simulador entra como lo haria una libreria de terceros.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "pasarela.sandbox", name = "habilitada", havingValue = "true")
@EnableConfigurationProperties(PasarelaSandboxProperties.class)
@Import({PasarelaSandboxController.class, PasarelaSandboxErrores.class})
public class PasarelaSandboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EntregaWebhook entregaWebhook() {
        return new EntregaWebhookHttp();
    }

    @Bean
    public PasarelaSandboxService pasarelaSandboxService(PasarelaSandboxProperties propiedades,
                                                         EntregaWebhook entrega, ObjectMapper json) {
        return new PasarelaSandboxService(propiedades, entrega, json, Clock.systemUTC());
    }
}
