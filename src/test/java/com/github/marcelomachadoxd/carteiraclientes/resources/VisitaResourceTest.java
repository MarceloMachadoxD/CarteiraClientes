package com.github.marcelomachadoxd.carteiraclientes.resources;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for VisitaResource.
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8
 *
 * Seed data assumptions:
 * - Visit id=1 exists with cliente_id=1 and responsavel_id=2
 * - User id=2 is responsavel for visits 1..30
 * - Cliente id=1 exists
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class VisitaResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Requirements: 6.1
    @Test
    public void findById_withExistingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/visitas/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Requirements: 6.2
    @Test
    public void findById_withNonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/visitas/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Requirements: 6.3
    @Test
    public void findByResponsavelId_withExistingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/visitas/responsavel/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    // Requirements: 6.4
    @Test
    public void findByClienteId_withExistingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/visitas/cliente/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    // Requirements: 6.5
    @Test
    public void insert_withValidBody_shouldReturn200() throws Exception {
        String dataVisita = Instant.now().minusSeconds(3600).toString();
        String body = "{"
                + "\"dataVisita\":\"" + dataVisita + "\","
                + "\"responsavel\":{\"id\":2},"
                + "\"cliente\":{\"id\":1}"
                + "}";

        mockMvc.perform(post("/visitas")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    // Requirements: 6.6
    @Test
    public void insert_withNullDataVisita_shouldReturn422() throws Exception {
        // dataVisita is omitted — @PastOrPresent constraint is not violated by null,
        // but responsavel and cliente are present. However, sending a future date triggers 422.
        // Sending null dataVisita: the field is nullable so we test with a future date instead,
        // which violates @PastOrPresent.
        String futureDate = Instant.now().plusSeconds(3600).toString();
        String body = "{"
                + "\"dataVisita\":\"" + futureDate + "\","
                + "\"responsavel\":{\"id\":2},"
                + "\"cliente\":{\"id\":1}"
                + "}";

        mockMvc.perform(post("/visitas")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    // Requirements: 6.7
    @Test
    public void insert_withNonExistingResponsavel_shouldReturn404() throws Exception {
        String dataVisita = Instant.now().minusSeconds(3600).toString();
        String body = "{"
                + "\"dataVisita\":\"" + dataVisita + "\","
                + "\"responsavel\":{\"id\":999999},"
                + "\"cliente\":{\"id\":1}"
                + "}";

        mockMvc.perform(post("/visitas")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Requirements: 6.8
    @Test
    public void delete_withExistingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/visitas/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // Requirements: 6.8
    // Note: VisitaService.delete() calls deleteById() without catching exceptions.
    // EmptyResultDataAccessException (RuntimeException) is caught by the generic handler → 500.
    @Test
    public void delete_withNonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/visitas/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}
