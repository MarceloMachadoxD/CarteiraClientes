package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserResource endpoints.
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.8, 7.9
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // GET /users
    // -------------------------------------------------------------------------

    @Test
    void findAllPageable_shouldReturn200WithPage() throws Exception {
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // -------------------------------------------------------------------------
    // GET /users/{id}
    // -------------------------------------------------------------------------

    @Test
    void findById_withExistingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void findById_withNonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/users/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /users
    // -------------------------------------------------------------------------

    @Test
    void insert_withValidBody_shouldReturn200() throws Exception {
        String body = "{\"nome\":\"Test\",\"email\":\"test@test.com\",\"password\":\"123456\",\"acessoId\":1}";

        mockMvc.perform(post("/users")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Test"));
    }

    @Test
    void insert_withMissingFields_shouldReturn422() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/users")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    // -------------------------------------------------------------------------
    // DELETE /users/{id}
    // -------------------------------------------------------------------------

    @Test
    void delete_withExistingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_withNonExistingId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/users/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
