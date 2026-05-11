package com.github.marcelomachadoxd.carteiraclientes.resources;

import tools.jackson.databind.ObjectMapper;
import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDadosBasicosDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.UserInsertDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração de roteamento para VisitaResource e UserResource.
 *
 * Verifica que os status HTTP de todos os endpoints de /visitas e /users
 * são os mesmos de antes da refatoração (extração das anotações Swagger para
 * interfaces contratuais).
 *
 * Requisitos: 1.10, 1.11, 7.1, 7.3
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VisitaUserRoutingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private VisitaDTO validVisitaDTO() {
        VisitaDTO dto = new VisitaDTO();
        dto.setDataVisita(Instant.parse("2025-03-15T14:30:00Z"));
        dto.setObs("Teste de visita");
        dto.setSatisfacao(true);

        UserDTO responsavel = new UserDTO();
        responsavel.setId(2L);
        dto.setResponsavel(responsavel);

        ClienteDadosBasicosDTO cliente = new ClienteDadosBasicosDTO();
        cliente.setId(1L);
        dto.setCliente(cliente);

        return dto;
    }

    private UserInsertDTO validUserInsertDTO() {
        UserInsertDTO dto = new UserInsertDTO();
        dto.setNome("Novo Corretor");
        dto.setEmail("novo.corretor@email.com");
        dto.setAcessoId(1L);
        dto.setPassword("senha123");
        return dto;
    }

    // -------------------------------------------------------------------------
    // VisitaResource — 6 endpoints
    // -------------------------------------------------------------------------

    /**
     * GET /visitas/1 → 200
     * Requisitos: 1.10, 7.1
     */
    @Test
    void visitas_getById_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/visitas/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * GET /visitas/responsavel/2 → 200
     * Requisitos: 1.10, 7.1
     */
    @Test
    void visitas_getByResponsavelId_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/visitas/responsavel/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * GET /visitas/cliente/1 → 200
     * Requisitos: 1.10, 7.1
     */
    @Test
    void visitas_getByClienteId_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/visitas/cliente/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * GET /visitas → 200
     * Requisitos: 1.10, 7.1
     */
    @Test
    void visitas_getAll_shouldReturn200() throws Exception {
        mockMvc.perform(get("/visitas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * POST /visitas com payload válido → 200
     * Requisitos: 1.10, 7.1
     */
    @Test
    void visitas_post_validPayload_shouldReturn200() throws Exception {
        String body = objectMapper.writeValueAsString(validVisitaDTO());
        mockMvc.perform(post("/visitas")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * DELETE /visitas/{novoId} → 204
     *
     * Cria uma nova visita via POST e deleta o ID retornado no corpo da resposta,
     * evitando que a remoção da visita id=1 afete outros testes.
     *
     * Requisitos: 1.10, 7.1
     */
    @Test
    void visitas_delete_newlyCreatedId_shouldReturn204() throws Exception {
        // 1. Cria uma nova visita e extrai o ID do corpo da resposta
        String body = objectMapper.writeValueAsString(validVisitaDTO());
        MvcResult createResult = mockMvc.perform(post("/visitas")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        VisitaDTO created = objectMapper.readValue(responseBody, VisitaDTO.class);
        long newId = created.getId();

        // 2. Deleta a visita recém-criada
        mockMvc.perform(delete("/visitas/{id}", newId))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // UserResource — 4 endpoints
    // -------------------------------------------------------------------------

    /**
     * GET /users → 200
     * Requisitos: 1.11, 7.1
     */
    @Test
    void users_getAll_shouldReturn200() throws Exception {
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * GET /users/2 → 200
     * Requisitos: 1.11, 7.1
     */
    @Test
    void users_getById_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/users/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * POST /users com payload válido → 200
     * Requisitos: 1.11, 7.1
     */
    @Test
    void users_post_validPayload_shouldReturn200() throws Exception {
        String body = objectMapper.writeValueAsString(validUserInsertDTO());
        mockMvc.perform(post("/users")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * DELETE /users/{novoId} → 204
     *
     * Cria um novo user via POST e deleta o ID retornado no corpo da resposta,
     * evitando que a remoção do user id=2 afete outros testes.
     *
     * Requisitos: 1.11, 7.1
     */
    @Test
    void users_delete_newlyCreatedId_shouldReturn204() throws Exception {
        // 1. Cria um novo user e extrai o ID do corpo da resposta
        String body = objectMapper.writeValueAsString(validUserInsertDTO());
        MvcResult createResult = mockMvc.perform(post("/users")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        UserDTO created = objectMapper.readValue(responseBody, UserDTO.class);
        long newId = created.getId();

        // 2. Deleta o user recém-criado
        mockMvc.perform(delete("/users/{id}", newId))
                .andExpect(status().isNoContent());
    }
}
