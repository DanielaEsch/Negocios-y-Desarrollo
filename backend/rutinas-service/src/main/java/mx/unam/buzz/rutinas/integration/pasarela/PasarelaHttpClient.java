package mx.unam.buzz.rutinas.integration.pasarela;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cliente HTTP de la pasarela. Solo manda el token y el monto: el numero de
 * tarjeta nunca pasa por aqui.
 */
@Slf4j
@Component
public class PasarelaHttpClient implements IPasarelaPagos {

    private final RestClient cliente;
    private final ObjectMapper json;

    public PasarelaHttpClient(RestClient.Builder builder, PagosProperties propiedades, ObjectMapper json) {
        this.cliente = builder
                .baseUrl(propiedades.pasarela().url())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + propiedades.pasarela().llaveSecreta())
                .build();
        this.json = json;
    }

    @Override
    public CargoPasarela crearCargo(String token, BigDecimal monto, String moneda, String referencia,
                                    String descripcion) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("token", token);
        cuerpo.put("monto", monto);
        cuerpo.put("moneda", moneda);
        cuerpo.put("referencia", referencia);
        cuerpo.put("descripcion", descripcion);
        try {
            JsonNode respuesta = cliente.post()
                    .uri("/cargos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
            if (respuesta == null) {
                throw new PasarelaException(false, "sin_respuesta", "La pasarela respondio vacio");
            }
            return new CargoPasarela(respuesta.path("id").asText(), respuesta.path("estado").asText(),
                    respuesta.at("/tarjeta/marca").asText(null), respuesta.at("/tarjeta/ultimos4").asText(null));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                JsonNode error = leer(ex.getResponseBodyAsString()).path("error");
                throw new PasarelaException(true, error.path("codigo").asText("rechazado"),
                        error.path("mensaje").asText("La pasarela rechazo el cargo"));
            }
            log.error("La pasarela fallo con {}", ex.getStatusCode());
            throw new PasarelaException(false, "error_pasarela", "La pasarela no pudo procesar el cargo");
        } catch (RestClientException ex) {
            log.error("No se pudo contactar a la pasarela: {}", ex.getMessage());
            throw new PasarelaException(false, "sin_conexion", "No se pudo contactar a la pasarela");
        }
    }

    private JsonNode leer(String cuerpo) {
        try {
            return json.readTree(cuerpo);
        } catch (Exception ex) {
            return json.createObjectNode();
        }
    }
}
