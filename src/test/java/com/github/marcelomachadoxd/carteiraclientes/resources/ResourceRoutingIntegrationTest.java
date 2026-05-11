package com.github.marcelomachadoxd.carteiraclientes.resources;

import tools.jackson.databind.ObjectMapper;
import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.RoleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração de roteamento para ClienteResource e RoleResource.
 *
 * Verifica que os status HTTP de todos os endpoints de /clientes e /roles
 * são os mesmos de antes da refatoração (extração das anotações Swagger para
 * interfaces contratuais).
 *
 * Requisitos: 1.9, 1.12, 7.1, 7.3
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResourceRoutingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ClienteDTO validClienteDTO() {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Teste");
        dto.setEmail("teste@email.com");
        dto.setQtdQuartos(2);
        dto.setQtdBanheiros(1);
        dto.setQtdVagas(1);
        dto.setMetragem(65);
        dto.setValorMaximo(350000);
        dto.setObs("Teste");
        return dto;
    }

    private ClienteDTO updateClienteDTO() {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Cliente Atualizado");
        dto.setEmail("cliente@email.com");
        dto.setQtdQuartos(2);
        dto.setQtdBanheiros(1);
        dto.setQtdVagas(1);
        dto.setMetragem(65);
        dto.setValorMaximo(350000);
        dto.setObs("Atualizado");
        return dto;
    }

    // -------------------------------------------------------------------------
    // ClienteResource — 6 endpoints
    // -------------------------------------------------------------------------

    /**
     * GET /clientes/id/1 → 200
     * Requisitos: 1.9, 7.1
     */
    @Test
    void clientes_getById_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/clientes/id/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * GET /clientes/nome/Cliente → 200
     * Requisitos: 1.9, 7.1
     */
    @Test
    void clientes_getByNome_existingNome_shouldReturn200() throws Exception {
        mockMvc.perform(get("/clientes/nome/{nome}", "Cliente")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * GET /clientes → 200
     * Requisitos: 1.9, 7.1
     */
    @Test
    void clientes_getAll_shouldReturn200() throws Exception {
        mockMvc.perform(get("/clientes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * POST /clientes com payload válido → 201
     * Requisitos: 1.9, 7.1
     */
    @Test
    void clientes_post_validPayload_shouldReturn201() throws Exception {
        String body = objectMapper.writeValueAsString(validClienteDTO());
        mockMvc.perform(post("/clientes")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    /**
     * PUT /clientes/id/1 com payload válido → 200
     * Requisitos: 1.9, 7.1
     */
    @Test
    void clientes_put_existingId_validPayload_shouldReturn200() throws Exception {
        String body = objectMapper.writeValueAsString(updateClienteDTO());
        mockMvc.perform(put("/clientes/id/{id}", 1L)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * DELETE /clientes/id/{novoId} → 204
     *
     * Cria um novo cliente via POST e deleta o ID retornado no header Location,
     * evitando que a remoção do cliente id=1 afete outros testes.
     *
     * Requisitos: 1.9, 7.1
     */
    @Test
    void clientes_delete_newlyCreatedId_shouldReturn204() throws Exception {
        // 1. Cria um novo cliente e extrai o ID do header Location
        String body = objectMapper.writeValueAsString(validClienteDTO());
        MvcResult createResult = mockMvc.perform(post("/clientes")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        // Location: http://localhost/clientes/id/{id}
        long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        // 2. Deleta o cliente recém-criado
        mockMvc.perform(delete("/clientes/id/{id}", newId))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // RoleResource — 2 endpoints
    // -------------------------------------------------------------------------

    /**
     * GET /roles → 200
     * Requisitos: 1.12, 7.1
     */
    @Test
    void roles_getAll_shouldReturn200() throws Exception {
        mockMvc.perform(get("/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * POST /roles com payload válido → 201
     * Requisitos: 1.12, 7.1
     */
    @Test
    void roles_post_validPayload_shouldReturn201() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setNome("ROLE_TESTE");
        String body = objectMapper.writeValueAsString(roleDTO);

        mockMvc.perform(post("/roles")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }
}
