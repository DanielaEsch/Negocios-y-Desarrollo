package mx.unam.buzz.rutinas.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Levanta el servidor real para comprobar el prefijo /api/v1, que MockMvc no aplica.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiPrefixTest {

    @Autowired
    private TestRestTemplate rest;

    @LocalServerPort
    private int port;

    /** URL absoluta: TestRestTemplate antepone el context-path a las rutas relativas. */
    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void lasRutasCuelganDeApiV1() {
        assertThat(rest.getForEntity(url("/api/v1/areas"), String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity(url("/areas"), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void swaggerUiEnApiDocs() {
        ResponseEntity<String> ui = rest.getForEntity(url("/api/v1/api-docs"), String.class);
        assertThat(ui.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ui.getBody()).contains("Swagger UI");

        ResponseEntity<String> contrato = rest.getForEntity(url("/api/v1/api-docs/openapi.json"), String.class);
        assertThat(contrato.getHeaders().getContentType()).isNotNull();
        assertThat(contrato.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(contrato.getBody()).contains("\"/areas\"", "/api/v1");
    }
}
