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
     * O JSON retornado DEVE conter campos internos do PageImpl que evidenciam
     * a serialização direta (bug confirmado).
     *
     * Campos internos do PageImpl presentes no JSON (não fazem parte de um DTO limpo):
     * - "pageable": objeto com offset, pageNumber, pageSize, paged, sort, unpaged
     * - "sort": objeto com empty, sorted, unsorted (duplicado — aparece em pageable e raiz)
     * - "numberOfElements": contagem de elementos na página atual
     * - "empty": boolean indicando se a página está vazia
     *
     * Counterexample: GET /clientes → JSON contém "pageable" e "numberOfElements"
     * (campos internos do PageImpl que não devem aparecer em um DTO estável)
     *
     * Validates: Requirements 1.3
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
                // Campos esperados de paginação (presentes tanto em PageImpl quanto em PageResponse)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.size").exists())
                // Campos INTERNOS do PageImpl — evidência do bug (serialização direta)
                // Estes campos NÃO devem aparecer em um DTO limpo (PageResponse<T>)
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.numberOfElements").exists())
                .andExpect(jsonPath("$.empty").exists())
                .andExpect(jsonPath("$.sort").exists());
    }

    /**
     * Bug Condition — GET /users (endpoint paginado com Page<UserDTO>):
     * O JSON retornado DEVE conter campos internos do PageImpl que evidenciam
     * a serialização direta (bug confirmado).
     *
     * Counterexample: GET /users → JSON contém "pageable" e "numberOfElements"
     * (campos internos do PageImpl que não devem aparecer em um DTO estável)
     *
     * Validates: Requirements 1.3
     */
    @Test
    void bugCondition_getUsers_paginado_jsonContemCamposInternosDoPageImpl() throws Exception {
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Campos esperados de paginação
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.size").exists())
                // Campos INTERNOS do PageImpl — evidência do bug (serialização direta)
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.numberOfElements").exists())
                .andExpect(jsonPath("$.empty").exists())
                .andExpect(jsonPath("$.sort").exists());
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
