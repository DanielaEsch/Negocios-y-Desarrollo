package mx.unam.buzz.rutinas.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fotos de evidencia: el operador de la ronda sube, el supervisor del area ve y
 * descarga, y nadie mas.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(SesionesDePrueba.class)
class EvidenciaFlujoTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private SesionesDePrueba sesiones;

    private final ObjectMapper json = new ObjectMapper();
    private MockMvc mockMvc;
    private String admin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)).build();
        admin = sesiones.crear(Perfil.ADMINISTRADOR, List.of()).bearer();
    }

    private static byte[] jpeg() {
        byte[] bytes = new byte[1024];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xD8;
        bytes[2] = (byte) 0xFF;
        return bytes;
    }

    @Test
    void elOperadorSubeYElSupervisorVe() throws Exception {
        long area = body(perform(post("/areas"), admin,
                "{\"nombre\": \"" + UUID.randomUUID().toString().substring(0, 8) + "\"}")).at("/data/id").asLong();
        String operador = sesiones.crear(Perfil.OPERADOR, List.of(area)).bearer();
        String companero = sesiones.crear(Perfil.OPERADOR, List.of(area)).bearer();
        String ajeno = sesiones.crear(Perfil.SUPERVISOR, List.of()).bearer();
        String supervisor = sesiones.crear(Perfil.SUPERVISOR, List.of(area)).bearer();
        long rutina = body(perform(post("/rutinas"), admin, """
                {"idArea": %d, "nombre": "Derrame", "frecuencia": "POR_EVENTO", "tareas": ["Limpiar"]}"""
                .formatted(area))).at("/data/id").asLong();
        long ronda = body(perform(post("/rutinas/" + rutina + "/ejecuciones"), operador, "")).at("/data/id").asLong();
        String evidencias = "/ejecuciones/" + ronda + "/evidencias";

        // Antes de tomar la ronda no se sube nada
        subir(evidencias, operador, jpeg(), "antes").andExpect(status().isBadRequest());
        perform(patch("/ejecuciones/" + ronda), operador, "{\"estado\": \"EN_CURSO\", \"version\": 0}")
                .andExpect(status().isOk());

        JsonNode foto = body(subir(evidencias, operador, jpeg(), "Piso seco")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tipoContenido").value("image/jpeg"))
                .andExpect(jsonPath("$.data.nota").value("Piso seco")));

        // Un PDF renombrado a .jpg no pasa; tampoco sube quien no tomo la ronda
        subir(evidencias, operador, "%PDF-1.7 no soy foto".getBytes(StandardCharsets.US_ASCII), null)
                .andExpect(status().isBadRequest());
        subir(evidencias, companero, jpeg(), null).andExpect(status().isBadRequest());

        perform(get(evidencias), supervisor, null).andExpect(jsonPath("$.data.length()").value(1));
        perform(get(evidencias), ajeno, null).andExpect(status().isForbidden());
        perform(get("/evidencias/" + foto.at("/data/id").asLong() + "/archivo"), supervisor, null)
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.startsWith("image/jpeg")))
                .andExpect(content().bytes(jpeg()));
        perform(get("/evidencias/" + foto.at("/data/id").asLong() + "/archivo"), ajeno, null)
                .andExpect(status().isForbidden());
    }

    private ResultActions subir(String url, String bearer, byte[] contenido, String nota) throws Exception {
        MockHttpServletRequestBuilder request = multipart(url)
                .file(new MockMultipartFile("archivo", "foto.jpg", MediaType.IMAGE_JPEG_VALUE, contenido))
                .header(HttpHeaders.AUTHORIZATION, bearer);
        if (nota != null) {
            request.param("nota", nota);
        }
        return mockMvc.perform(request);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, String bearer, String content)
            throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, bearer);
        if (content != null && !content.isEmpty()) {
            request.contentType(MediaType.APPLICATION_JSON).content(content);
        }
        return mockMvc.perform(request);
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
}
