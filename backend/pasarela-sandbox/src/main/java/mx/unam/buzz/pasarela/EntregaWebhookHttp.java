package mx.unam.buzz.pasarela;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Entrega real: POST con el evento en JSON y la firma en la cabecera Pasarela-Firma.
 */
public class EntregaWebhookHttp implements EntregaWebhook {

    private static final Logger LOG = LoggerFactory.getLogger(EntregaWebhookHttp.class);

    private final RestClient cliente = RestClient.create();

    @Override
    public boolean entregar(String url, String cuerpo, String firma) {
        try {
            cliente.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(FirmaWebhook.CABECERA, firma)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException ex) {
            LOG.warn("[sandbox] el comercio no acepto el webhook: {}", ex.getMessage());
            return false;
        }
    }
}
