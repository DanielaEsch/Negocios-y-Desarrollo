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
                .andExpect(jsonPath("$.message").value("El área no existe o fue eliminada."));
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
                .andExpect(jsonPath("$.code").value(1001));
    }
}
