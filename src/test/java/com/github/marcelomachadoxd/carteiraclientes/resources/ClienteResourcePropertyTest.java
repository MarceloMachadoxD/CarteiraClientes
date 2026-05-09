package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Property-based tests for ClienteResource endpoints.
 *
 * Uses @SpringBootTest + @AutoConfigureMockMvc + ApplicationContextAware pattern
 * to ensure Spring beans are available in jqwik @Property methods.
 *
 * Requirements: 9.2, 9.3, 9.4, 9.5, 9.6
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteResourcePropertyTest implements ApplicationContextAware {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    private static MockMvc sharedMockMvc;
    private static ObjectMapper sharedObjectMapper;
    private static ClienteRepository sharedClienteRepository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        sharedMockMvc = applicationContext.getBean(MockMvc.class);
        sharedObjectMapper = applicationContext.getBean(ObjectMapper.class);
        sharedClienteRepository = applicationContext.getBean(ClienteRepository.class);
    }

    private MockMvc getMockMvc() {
        if (mockMvc != null) return mockMvc;
        if (sharedMockMvc != null) return sharedMockMvc;
        try {
            TestContextManager tcm = new TestContextManager(ClienteResourcePropertyTest.class);
            tcm.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return mockMvc;
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper != null) return objectMapper;
        if (sharedObjectMapper != null) return sharedObjectMapper;
        try {
            TestContextManager tcm = new TestContextManager(ClienteResourcePropertyTest.class);
            tcm.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return objectMapper;
    }

    private ClienteRepository getClienteRepository() {
        if (clienteRepository != null) return clienteRepository;
        if (sharedClienteRepository != null) return sharedClienteRepository;
        try {
            TestContextManager tcm = new TestContextManager(ClienteResourcePropertyTest.class);
            tcm.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return clienteRepository;
    }

    /**
     * Helper method to build a valid ClienteDTO with the given parameters.
     */
    private ClienteDTO buildValidClienteDTO(String nome, String email, int qtdQuartos,
                                             int qtdBanheiros, int qtdVagas,
                                             int metragem, int valorMaximo) {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome(nome);
        dto.setEmail(email);
        dto.setQtdQuartos(qtdQuartos);
        dto.setQtdBanheiros(qtdBanheiros);
        dto.setQtdVagas(qtdVagas);
        dto.setMetragem(metragem);
        dto.setValorMaximo(valorMaximo);
        return dto;
    }

    // Feature: spring-boot-4-java-upgrade, Property 1: Busca por ID existente retorna DTO completo
    @Property(tries = 100)
    void findById_existingId_returns200WithAllFields(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String nome,
            @ForAll @IntRange(min = 0, max = 10) int qtdQuartos,
            @ForAll @IntRange(min = 0, max = 10) int qtdBanheiros,
            @ForAll @IntRange(min = 0, max = 10) int qtdVagas,
            @ForAll @IntRange(min = 0, max = 10) int metragem,
            @ForAll @IntRange(min = 0, max = 10) int valorMaximo) throws Exception {

        ClienteRepository repo = getClienteRepository();

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(System.nanoTime() + "@pbt.test");
        cliente.setQtdQuartos(qtdQuartos);
        cliente.setQtdBanheiros(qtdBanheiros);
        cliente.setQtdVagas(qtdVagas);
        cliente.setMetragem(metragem);
        cliente.setValorMaximo(valorMaximo);
        Cliente saved = repo.save(cliente);
        repo.flush();

        getMockMvc().perform(get("/clientes/id/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.qtdQuartos").exists())
                .andExpect(jsonPath("$.qtdBanheiros").exists())
                .andExpect(jsonPath("$.qtdVagas").exists())
                .andExpect(jsonPath("$.metragem").exists())
                .andExpect(jsonPath("$.valorMaximo").exists());
    }

    // Feature: spring-boot-4-java-upgrade, Property 2: Busca por ID inexistente retorna 404
    @Property(tries = 100)
    void findById_nonExistingId_returns404WithError(
            @ForAll @LongRange(min = 900000L, max = 999999L) long id) throws Exception {

        getMockMvc().perform(get("/clientes/id/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // Feature: spring-boot-4-java-upgrade, Property 3: Criação com payload válido retorna 201 com Location
    @Property(tries = 100)
    void insert_validPayload_returns201WithLocation(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String nome,
            @ForAll @IntRange(min = 0, max = 10) int qtdQuartos,
            @ForAll @IntRange(min = 0, max = 10) int qtdBanheiros,
            @ForAll @IntRange(min = 0, max = 10) int qtdVagas,
            @ForAll @IntRange(min = 0, max = 10) int metragem,
            @ForAll @IntRange(min = 0, max = 10) int valorMaximo) throws Exception {

        String uniqueEmail = System.nanoTime() + "@pbt.test";
        ClienteDTO dto = buildValidClienteDTO(nome, uniqueEmail, qtdQuartos,
                qtdBanheiros, qtdVagas, metragem, valorMaximo);

        getMockMvc().perform(post("/clientes")
                        .content(getObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    // Feature: spring-boot-4-java-upgrade, Property 4: Criação com email inválido retorna 422 com erros de validação
    @Property(tries = 100)
    void insert_invalidEmail_returns422WithEmailError(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String invalidEmail) throws Exception {

        // AlphaChars strings never contain '@', so they are always invalid emails
        ClienteDTO dto = buildValidClienteDTO("Nome", invalidEmail, 2, 1, 1, 60, 300000);

        getMockMvc().perform(post("/clientes")
                        .content(getObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].fieldName").value("email"));
    }

    // Feature: spring-boot-4-java-upgrade, Property 5: Atualização com payload válido retorna 200
    @Property(tries = 100)
    void update_validPayload_returns200(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String nome,
            @ForAll @IntRange(min = 0, max = 10) int qtdQuartos,
            @ForAll @IntRange(min = 0, max = 10) int qtdBanheiros,
            @ForAll @IntRange(min = 0, max = 10) int qtdVagas,
            @ForAll @IntRange(min = 0, max = 10) int metragem,
            @ForAll @IntRange(min = 0, max = 10) int valorMaximo) throws Exception {

        ClienteRepository repo = getClienteRepository();

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(System.nanoTime() + "@pbt.test");
        cliente.setQtdQuartos(qtdQuartos);
        cliente.setQtdBanheiros(qtdBanheiros);
        cliente.setQtdVagas(qtdVagas);
        cliente.setMetragem(metragem);
        cliente.setValorMaximo(valorMaximo);
        Cliente saved = repo.save(cliente);
        repo.flush();

        String uniqueEmail = System.nanoTime() + "@pbt.updated";
        ClienteDTO updateDto = buildValidClienteDTO(nome, uniqueEmail, qtdQuartos,
                qtdBanheiros, qtdVagas, metragem, valorMaximo);

        getMockMvc().perform(put("/clientes/id/{id}", saved.getId())
                        .content(getObjectMapper().writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
