package com.github.marcelomachadoxd.carteiraclientes.resources;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de exploração da condição do bug — Bug 3: Serialização de PageImpl.
 *
 * OBJETIVO: Confirmar que os endpoints paginados estão serializando PageImpl diretamente,
 * o que é evidenciado pela presença de campos internos do PageImpl no JSON retornado
 * (como "pageable", "sort", "numberOfElements", "empty") que não fazem parte de um DTO
 * limpo e cuja estrutura não é garantida como estável entre versões do Spring.
 *
 * O warning "Serializing PageImpl instances as-is is not supported" é emitido pelo Spring
 * Data apenas uma vez por JVM (na primeira serialização), portanto a abordagem mais
 * confiável para confirmar o bug é verificar a estrutura do JSON retornado.
 *
 * Counterexamples documentados:
 * - "GET /clientes com ClienteResource retornando Page<ClienteDTO>
 *    → JSON contém campos internos do PageImpl: 'pageable', 'sort', 'numberOfElements', 'empty'"
 * - "GET /users com UserResource retornando Page<UserDTO>
 *    → JSON contém campos internos do PageImpl: 'pageable', 'sort', 'numberOfElements', 'empty'"
 * - "GET /roles com RoleResource retornando List<RoleDTO>
 *    → JSON é um array simples, sem campos internos de paginação"
 *
 * Validates: Requirements 1.3
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PageImplSerializationBugExplorationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Bug Condition — GET /clientes (endpoint paginado com Page<ClienteDTO>):
     * APÓS A CORREÇÃO (tarefa 5.2): ClienteResource agora retorna PageResponse<ClienteDTO>,
     * portanto o JSON NÃO deve conter campos internos do PageImpl.
     *
     * Este teste foi originalmente escrito para confirmar o bug (presença de campos internos).
     * Após a correção do ClienteResource, o teste verifica o comportamento esperado:
     * - Campos do PageResponse presentes: content, totalElements, totalPages, number, size, first, last
     * - Campos internos do PageImpl AUSENTES: pageable, numberOfElements, empty, sort
     *
     * Validates: Requirements 1.3, 2.3
     */
    @Test
    void bugCondition_getClientes_paginado_jsonContemCamposInternosDoPageImpl() throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("margem", "0")
                        .param("qtdQuartos", "0")
                        .param("qtdBanheiros", "0")
                        .param("qtdVagas", "0")
                        .param("metragem", "0")
                        .param("valorMaximo", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Campos do PageResponse (presentes após a correção)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                // Campos INTERNOS do PageImpl — NÃO devem existir após a correção
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    /**
     * Bug Condition — GET /users (endpoint paginado com Page<UserDTO>):
     * APÓS A CORREÇÃO (tarefa 5.4): UserResource agora retorna PageResponse<UserDTO>,
     * portanto o JSON NÃO deve conter campos internos do PageImpl.
     *
     * Este teste foi originalmente escrito para confirmar o bug (presença de campos internos).
     * Após a correção do UserResource, o teste verifica o comportamento esperado:
     * - Campos do PageResponse presentes: content, totalElements, totalPages, number, size, first, last
     * - Campos internos do PageImpl AUSENTES: pageable, numberOfElements, empty, sort
     *
     * Validates: Requirements 1.3, 2.3
     */
    @Test
    void bugCondition_getUsers_paginado_jsonContemCamposInternosDoPageImpl() throws Exception {
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Campos do PageResponse (presentes após a correção)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists())
                // Campos INTERNOS do PageImpl — NÃO devem existir após a correção
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.numberOfElements").doesNotExist())
                .andExpect(jsonPath("$.empty").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    /**
     * Não-condição do bug — GET /roles (endpoint NÃO paginado com List<RoleDTO>):
     * O JSON retornado é um array simples, sem campos de paginação.
     * Confirma que a condição do bug não se aplica a endpoints não paginados.
     *
     * Validates: Requirements 1.3
     */
    @Test
    void naoCondicaoDoBug_getRoles_naoPaginado_jsonEhArraySimples() throws Exception {
        mockMvc.perform(get("/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Resposta é um array JSON simples (List<RoleDTO>), não um objeto paginado
                .andExpect(jsonPath("$").isArray())
                // Campos de paginação NÃO devem existir (não é paginado)
                .andExpect(jsonPath("$.content").doesNotExist())
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.totalElements").doesNotExist());
    }
}
