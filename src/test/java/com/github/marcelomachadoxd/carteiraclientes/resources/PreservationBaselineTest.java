package com.github.marcelomachadoxd.carteiraclientes.resources;

import tools.jackson.databind.ObjectMapper;
import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de preservação (baseline) — Tarefa 2 do bugfix test-warnings-and-pageimpl-fix.
 *
 * Estes testes capturam o comportamento observado no código NÃO corrigido e devem
 * PASSAR antes e depois de qualquer correção ser aplicada (sem regressões).
 *
 * Seed data relevante:
 * - Cliente id=1, nome='Cliente'
 * - User id=2 é responsável pelas Visitas id=1..30 (30 visitas no total)
 * - Role id=1 existe (nome='ADMIN'), Role id=2 existe (nome='RESPONSAVEL')
 *
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PreservationBaselineTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // (a) GET /clientes/id/{id} — endpoint não paginado retorna 200 com ClienteDTO
    // Requirements: 3.2
    // -------------------------------------------------------------------------

    /**
     * Preservation (a): GET /clientes/id/1 deve retornar 200 OK com ClienteDTO correto.
     * Endpoint não paginado — não é afetado pela introdução de PageResponse<T>.
     */
    @Test
    void preservation_getClienteById_shouldReturn200WithCorrectClienteDTO() throws Exception {
        mockMvc.perform(get("/clientes/id/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Cliente"))
                .andExpect(jsonPath("$.email").value("cliente@email.com"));
    }

    // -------------------------------------------------------------------------
    // (b) GET /roles — endpoint não paginado retorna 200 com List<RoleDTO>
    // Requirements: 3.5
    // -------------------------------------------------------------------------

    /**
     * Preservation (b): GET /roles deve retornar 200 OK com List<RoleDTO> contendo
     * pelo menos 2 roles (ADMIN e RESPONSAVEL do seed data).
     * Endpoint não paginado — não é afetado pela introdução de PageResponse<T>.
     */
    @Test
    void preservation_getRoles_shouldReturn200WithRoleDTOList() throws Exception {
        mockMvc.perform(get("/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Resposta é um array JSON (List), não um objeto paginado
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nome").exists());
    }

    // -------------------------------------------------------------------------
    // (c) POST /clientes — retorna 201 Created
    // Requirements: 3.6
    // -------------------------------------------------------------------------

    /**
     * Preservation (c): POST /clientes com body válido deve retornar 201 Created
     * com header Location e o ClienteDTO criado no corpo.
     */
    @Test
    void preservation_postCliente_shouldReturn201Created() throws Exception {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Preservation Test Cliente");
        dto.setEmail("preservation.test@example.com");
        dto.setQtdQuartos(2);
        dto.setQtdBanheiros(1);
        dto.setQtdVagas(1);
        dto.setMetragem(60);
        dto.setValorMaximo(300000);

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/clientes")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Preservation Test Cliente"));
    }

    // -------------------------------------------------------------------------
    // (d) PUT /clientes/id/{id} — retorna 200 OK
    // Requirements: 3.6
    // -------------------------------------------------------------------------

    /**
     * Preservation (d): PUT /clientes/id/1 com body válido deve retornar 200 OK.
     */
    @Test
    void preservation_putCliente_shouldReturn200OK() throws Exception {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Cliente Atualizado");
        dto.setEmail("cliente.atualizado@example.com");
        dto.setQtdQuartos(3);
        dto.setQtdBanheiros(2);
        dto.setQtdVagas(2);
        dto.setMetragem(80);
        dto.setValorMaximo(500000);

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/clientes/id/{id}", 1L)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // (e) DELETE /clientes/id/{id} — retorna 204 No Content
    // Requirements: 3.6
    // -------------------------------------------------------------------------

    /**
     * Preservation (e): DELETE /clientes/id/1 deve retornar 204 No Content.
     */
    @Test
    void preservation_deleteCliente_shouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/clientes/id/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // (f) GET /clientes — paginado, verifica content e metadados de paginação
    // Requirements: 3.3, 3.4, 3.8
    // -------------------------------------------------------------------------

    /**
     * Preservation (f): GET /clientes com seed data deve retornar 200 OK com
     * content contendo os clientes esperados e metadados de paginação corretos.
     * O seed data tem 36 clientes. Com page=0&size=10, esperamos:
     * - totalElements = 36
     * - totalPages = 4
     * - number = 0
     * - size = 10
     * - first = true
     * - last = false
     * - content com 10 elementos
     */
    @Test
    void preservation_getClientes_shouldReturnPaginatedContentWithCorrectMetadata() throws Exception {
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
                .andExpect(jsonPath("$.totalElements").value(36))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(10)));
    }

    /**
     * Preservation (f-extra): GET /clientes com seed data, última página.
     * Com page=3&size=10, esperamos:
     * - number = 3
     * - last = true
     * - content com 6 elementos (36 - 3*10 = 6)
     */
    @Test
    void preservation_getClientes_lastPage_shouldHaveCorrectMetadata() throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("margem", "0")
                        .param("qtdQuartos", "0")
                        .param("qtdBanheiros", "0")
                        .param("qtdVagas", "0")
                        .param("metragem", "0")
                        .param("valorMaximo", "0")
                        .param("page", "3")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(3))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(6)));
    }

    /**
     * Preservation (f-empty): GET /clientes com filtros que não retornam resultados.
     * Quando não há registros, deve retornar 200 OK com content vazio,
     * totalElements=0, totalPages=0, first=true, last=true.
     *
     * A query findByInteresses busca clientes cujo valorMaximo seja >= ao valor buscado
     * (com margem). Para obter resultado vazio, usamos um valorMaximo muito alto
     * (999999999) que nenhum cliente do seed data atinge (máximo é 584205).
     * Requirements: 3.8
     */
    @Test
    void preservation_getClientes_withNoMatchingFilter_shouldReturnEmptyPage() throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("margem", "0")
                        .param("qtdQuartos", "0")
                        .param("qtdBanheiros", "0")
                        .param("qtdVagas", "0")
                        .param("metragem", "0")
                        // valorMaximo muito alto: nenhum cliente do seed tem valorMaximo >= 999999999
                        .param("valorMaximo", "999999999")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // (g) GET /visitas/responsavel/2 — verifica totalElements=30 e content correto
    // Requirements: 3.5
    // -------------------------------------------------------------------------

    /**
     * Preservation (g): GET /visitas/responsavel/2 com seed data deve retornar
     * totalElements=30 (User id=2 é responsável pelas Visitas id=1..30).
     * Com page=0&size=10, content deve ter 10 elementos.
     */
    @Test
    void preservation_getVisitasByResponsavel2_shouldReturn30TotalElements() throws Exception {
        mockMvc.perform(get("/visitas/responsavel/{id}", 2L)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(10)))
                // Cada elemento do content deve ter id e responsavel.id=2
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].responsavel.id").value(2));
    }

    /**
     * Preservation (g-extra): GET /visitas/responsavel/2 com page=2&size=10
     * deve retornar a última página com 10 elementos (30 visitas / 10 por página = 3 páginas).
     */
    @Test
    void preservation_getVisitasByResponsavel2_lastPage_shouldHaveCorrectMetadata() throws Exception {
        mockMvc.perform(get("/visitas/responsavel/{id}", 2L)
                        .param("page", "2")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(10)));
    }
}
