package mx.unam.buzz.rutinas.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.service.IEjecucionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El recorrido de la demo contra H2, por las cuatro capas: el administrador arma la
 * rutina, el operador hace la ronda, reporta y el supervisor resuelve. Comprueba el
 * bloqueo optimista (409), los permisos por perfil y por area (403) y que cada
 * perfil reciba su DTO.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(SesionesDePrueba.class)
class RondaFlujoTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SesionesDePrueba sesiones;

    @Autowired
    private IEjecucionService ejecucionService;

    private final ObjectMapper json = new ObjectMapper();

    private MockMvc mockMvc;
    private String admin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)).build();
        admin = sesiones.crear(Perfil.ADMINISTRADOR, List.of()).bearer();
    }

    @Test
    void rondaCompletaConIncidencia() throws Exception {
        long area = crearArea();
        String operador = sesiones.crear(Perfil.OPERADOR, List.of(area)).bearer();
        String companero = sesiones.crear(Perfil.OPERADOR, List.of(area)).bearer();
        String ajeno = sesiones.crear(Perfil.OPERADOR, List.of()).bearer();
        String supervisor = sesiones.crear(Perfil.SUPERVISOR, List.of(area)).bearer();

        // El administrador crea una rutina por evento con dos tareas
        JsonNode rutina = body(perform(post("/rutinas"), admin, """
                {"idArea": %d, "nombre": "Limpieza por derrame", "frecuencia": "POR_EVENTO",
                 "tareas": ["Senalizar el area", "Limpiar y desinfectar"]}""".formatted(area))
                .andExpect(status().isCreated()));
        assertThat(rutina.at("/data/tareas").size()).isEqualTo(2);

        // El operador la lanza y la toma; su companero llega tarde con la version vieja
        JsonNode ronda = body(perform(post("/rutinas/" + rutina.at("/data/id").asLong() + "/ejecuciones"), operador, "")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estado").value("PROGRAMADA")));
        String url = "/ejecuciones/" + ronda.at("/data/id").asLong();
        long tarea1 = ronda.at("/data/tareas/0/idTarea").asLong();
        long tarea2 = ronda.at("/data/tareas/1/idTarea").asLong();

        perform(patch(url), operador, estado("EN_CURSO", 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(1));
        perform(patch(url), companero, estado("EN_CURSO", 0))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1405));

        // Tareas: cada avance sube la version, y una version vieja se rechaza
        perform(patch(url + "/tareas/" + tarea1), operador, estado("COMPLETADA", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tareasCompletadas").value(1))
                .andExpect(jsonPath("$.data.version").value(2));
        perform(patch(url + "/tareas/" + tarea2), operador, estado("COMPLETADA", 1))
                .andExpect(status().isConflict());
        perform(patch(url), operador, estado("COMPLETADA", 2))
                .andExpect(status().isBadRequest());
        perform(patch(url + "/tareas/" + tarea2), operador, estado("COMPLETADA", 2))
                .andExpect(status().isOk());
        perform(patch(url), operador, estado("COMPLETADA", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("COMPLETADA"));

        // Quien no tiene el area no ve la ronda
        perform(get(url), ajeno, null).andExpect(status().isForbidden());

        // Incidencia: el operador reporta, no puede revisar; el supervisor revisa y resuelve
        JsonNode incidencia = body(perform(post(url + "/incidencias"), operador,
                "{\"descripcion\": \"Se acabo el desinfectante\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE")));
        String urlIncidencia = "/incidencias/" + incidencia.at("/data/id").asLong();
        perform(patch(urlIncidencia), operador, "{\"estado\": \"REVISION\"}")
                .andExpect(status().isForbidden());
        perform(patch(urlIncidencia), supervisor, "{\"estado\": \"RESUELTA\", \"comentarioResolucion\": \"x\"}")
                .andExpect(status().isConflict());
        perform(patch(urlIncidencia), supervisor, "{\"estado\": \"REVISION\"}")
                .andExpect(status().isOk());
        perform(patch(urlIncidencia), supervisor, "{\"estado\": \"RESUELTA\"}")
                .andExpect(status().isBadRequest());
        perform(patch(urlIncidencia), supervisor,
                "{\"estado\": \"RESUELTA\", \"comentarioResolucion\": \"Se repuso\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("RESUELTA"))
                .andExpect(jsonPath("$.data.reporta").exists());

        // DTO por perfil: el operador no recibe quien reporto ni quien reviso
        perform(get("/incidencias"), operador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].comentarioResolucion").value("Se repuso"))
                .andExpect(jsonPath("$.data[0].reporta").doesNotExist());

        // Tablero: solo quien supervisa
        perform(get("/tablero"), supervisor, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dia.completadas").value(1))
                .andExpect(jsonPath("$.data.dia.cumplimiento").value(100.0))
                .andExpect(jsonPath("$.data.tendencia.length()").value(7));
        perform(get("/tablero"), operador, null).andExpect(status().isForbidden());
    }

    @Test
    void laProgramacionUsaLaFrecuenciaYEsIdempotente() throws Exception {
        long area = crearArea();
        perform(post("/rutinas"), admin, """
                {"idArea": %d, "nombre": "Revision de banos", "frecuencia": "CADA_2_HORAS",
                 "horaInicio": "08:00", "horaFin": "20:00", "tareas": ["Revisar jabon"]}""".formatted(area))
                .andExpect(status().isCreated());
        perform(post("/rutinas"), admin, """
                {"idArea": %d, "nombre": "Sin hora de fin", "frecuencia": "CADA_2_HORAS",
                 "horaInicio": "08:00", "tareas": ["Revisar jabon"]}""".formatted(area))
                .andExpect(status().isBadRequest());

        LocalDate dia = LocalDate.of(2030, 1, 7);
        assertThat(ejecucionService.programarDia(dia, dia.atStartOfDay())).isGreaterThanOrEqualTo(7);
        assertThat(ejecucionService.programarDia(dia, dia.atStartOfDay())).isZero();

        perform(get("/ejecuciones").param("fecha", "2030-01-07").param("idArea", String.valueOf(area)), admin, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.data[0].fechaProgramada").value("2030-01-07T08:00:00"))
                .andExpect(jsonPath("$.data[6].fechaProgramada").value("2030-01-07T20:00:00"));
    }

    private long crearArea() throws Exception {
        return body(perform(post("/areas"), admin, "{\"nombre\": \"" + UUID.randomUUID().toString().substring(0, 8) + "\"}")
                .andExpect(status().isCreated())).at("/data/id").asLong();
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

    private static String estado(String estado, long version) {
        return "{\"estado\": \"" + estado + "\", \"version\": " + version + "}";
    }
}
