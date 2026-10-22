package mx.unam.buzz.rutinas.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorre las cuatro capas contra H2: controlador, fachada, servicio y repositorio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cicloCompletoDeUnArea() throws Exception {
        String id = mockMvc.perform(post("/areas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\" cocina \",\"descripcion\":\"alimentos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nombre").value("COCINA"))
                .andExpect(jsonPath("$.data.fechaCreacion").exists())
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(post("/areas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"COCINA\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:buzz-rutinas:problema:negocio"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value(1002));

        mockMvc.perform(put("/areas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"cocina central\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/areas").param("nombre", "central"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].nombre").value("COCINA CENTRAL"));

        mockMvc.perform(delete("/areas/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/areas/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1003))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("El área no existe o fue eliminada."))
                .andExpect(jsonPath("$.instance").value("/areas/" + id));
    }

    @Test
    void ordenamientoInvalido() throws Exception {
        mockMvc.perform(get("/areas").param("orderBy", "noexiste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void nombreObligatorio() throws Exception {
        mockMvc.perform(post("/areas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:buzz-rutinas:problema:validacion"))
                .andExpect(jsonPath("$.title").value("Error de Validación en los Datos de Entrada"))
                .andExpect(jsonPath("$.instance").value("/areas"))
                .andExpect(jsonPath("$.invalid_params[0].name").value("nombre"))
                .andExpect(jsonPath("$.invalid_params[0].reason").value("El nombre es obligatorio."));
    }

    @Test
    void largoMaximoDelNombre() throws Exception {
        mockMvc.perform(post("/areas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + "x".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params[0].name").value("nombre"))
                .andExpect(jsonPath("$.invalid_params[0].reason").value("El nombre admite como máximo 100 caracteres."));
    }

    @Test
    void campoDesconocidoSeRechaza() throws Exception {
        mockMvc.perform(post("/areas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"bodega\",\"activo\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:buzz-rutinas:problema:peticion-invalida"))
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void tamanoDePaginaFueraDeRango() throws Exception {
        mockMvc.perform(get("/areas").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:buzz-rutinas:problema:validacion"))
                .andExpect(jsonPath("$.invalid_params[0].name").value("size"))
                .andExpect(jsonPath("$.invalid_params[0].reason").value("El tamaño de página admite como máximo 100."));
    }

    @Test
    void rutaInexistenteTambienEsProblemDetail() throws Exception {
        mockMvc.perform(get("/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").value("/no-existe"));
    }

    @Test
    void swaggerEnApiDocs() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api-docs/openapi.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/areas']").exists())
                .andExpect(jsonPath("$.components.schemas.CreateAreaRequestVO.required[0]").value("nombre"))
                .andExpect(jsonPath("$.components.schemas.CreateAreaRequestVO.properties.nombre.maxLength").value(100))
                .andExpect(jsonPath("$.paths['/areas'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/areas'].post.responses['409'].content['application/problem+json']").exists());
    }
}
