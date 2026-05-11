package com.github.marcelomachadoxd.carteiraclientes.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração via MockMvc para verificar que ClienteResource,
 * VisitaResource e UserResource retornam {@link PageResponse} nos endpoints paginados.
 *
 * Verifica:
 * - campos obrigatórios presentes: content, totalElements, totalPages, number, size, first, last
 * - campos internos do PageImpl AUSENTES: pageable, numberOfElements, empty, sort
 *
 * Seed data relevante:
 * - Cliente id=1, nome='Cliente'
 * - User id=2 é responsável pelas Visitas id=1..30
 * - Visita id=1 tem cliente_id=1 e responsavel_id=2
 *
 * Requirements: 2.3, 3.3, 3.4, 3.5, 3.8
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PageResponseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // ClienteResource — endpoints paginados
    // =========================================================================

    /**
     * GET /clientes (findByInteresses) deve retornar PageResponse<ClienteDTO>:
     * campos first e last presentes; pageable/numberOfElements/empty/sort ausentes.
     */
    @Test
    void clienteResource_findByInteresses_shouldReturnPageResponseFields() throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("margem", "0")
                        .param("qtdQuartos", "0")
                        .param("qtdBanheiros", "0")
                        .param("qtdVagas", "0")
                        .param("metragem", "0")
                        .param("valorMaximo", "0")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Campos obrigatórios do PageResponse
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                // Campos internos do PageImpl que NÃO devem estar presentes
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    /**
     * GET /clientes/nome/{nome} (findByNome) deve retornar PageResponse<ClienteDTO>:
     * campos first e last presentes; pageable/numberOfElements/empty/sort ausentes.
     */
    @Test
    void clienteResource_findByNome_shouldReturnPageResponseFields() throws Exception {
        mockMvc.perform(get("/clientes/nome/{nome}", "Cliente")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    // =========================================================================
    // VisitaResource — endpoints paginados
    // =========================================================================

    /**
     * GET /visitas/responsavel/2 deve retornar PageResponse<VisitaDTO>:
     * campos first e last presentes; pageable/numberOfElements/empty/sort ausentes.
     */
    @Test
    void visitaResource_findByResponsavelId_shouldReturnPageResponseFields() throws Exception {
        mockMvc.perform(get("/visitas/responsavel/{id}", 2L)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    /**
     * GET /visitas/cliente/1 deve retornar PageResponse<VisitaDTO>:
     * campos first e last presentes; pageable/numberOfElements/empty/sort ausentes.
     */
    @Test
    void visitaResource_findByClienteId_shouldReturnPageResponseFields() throws Exception {
        mockMvc.perform(get("/visitas/cliente/{id}", 1L)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    /**
     * GET /visitas (findByClienteAndResponsavelId) deve retornar PageResponse<VisitaDTO>:
     * campos first e last presentes; pageable/numberOfElements/empty/sort ausentes.
     */
    @Test
    void visitaResource_findByClienteAndResponsavelId_shouldReturnPageResponseFields() throws Exception {
        mockMvc.perform(get("/visitas")
                        .param("cliId", "1")
                        .param("respId", "2")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    // =========================================================================
    // UserResource — endpoint paginado
    // =========================================================================

    /**
     * GET /users (findAllPageable) deve retornar PageResponse<UserDTO>:
     * campos first e last presentes; pageable/numberOfElements/empty/sort ausentes.
     */
    @Test
    void userResource_findAllPageable_shouldReturnPageResponseFields() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }
}
