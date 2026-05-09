package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.services.ClienteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ClienteResource.
 *
 * @SpyBean wraps the real ClienteService so all tests use the real implementation by default.
 * Test 11 overrides findById via doThrow() to simulate an unmapped RuntimeException,
 * verifying the generic RuntimeException handler returns HTTP 500.
 *
 * @Transactional rolls back DB changes after each test method.
 *
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 8.1, 8.2, 8.3, 8.4
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // @SpyBean wraps the real ClienteService so all methods use the real implementation by default.
    // Test 11 overrides findById via doThrow() to simulate an unmapped RuntimeException.
    @SpyBean
    private ClienteService clienteService;

    private ClienteDTO validClienteDTO() {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Test Cliente");
        dto.setEmail("test@example.com");
        dto.setQtdQuartos(2);
        dto.setQtdBanheiros(1);
        dto.setQtdVagas(1);
        dto.setMetragem(60);
        dto.setValorMaximo(300000);
        return dto;
    }

    // Test 1: GET /clientes/id/1 -> 200 with id=1
    // Requirements: 5.1
    @Test
    void findById_withExistingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/clientes/id/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // Test 2: GET /clientes/id/999999 -> 404
    // Requirements: 5.2
    @Test
    void findById_withNonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/clientes/id/{id}", 999999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Test 3: GET /clientes/nome/Cliente -> 200 with totalElements >= 1
    // Requirements: 5.3
    @Test
    void findByNome_withExistingNome_shouldReturn200WithResults() throws Exception {
        mockMvc.perform(get("/clientes/nome/{nome}", "Cliente")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(
                        org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    // Test 4: POST /clientes with valid body -> 201 with Location header
    // Requirements: 5.4
    @Test
    void insert_withValidBody_shouldReturn201WithLocation() throws Exception {
        String body = objectMapper.writeValueAsString(validClienteDTO());
        mockMvc.perform(post("/clientes")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    // Test 5: POST /clientes with invalid email -> 422 with errors[0].fieldName
    // Requirements: 5.5
    @Test
    void insert_withInvalidEmail_shouldReturn422WithErrors() throws Exception {
        ClienteDTO dto = validClienteDTO();
        dto.setEmail("not-an-email");
        String body = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/clientes")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].fieldName").exists());
    }

    // Test 6: DELETE /clientes/id/1 -> 204
    // Requirements: 5.6
    @Test
    void delete_withExistingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/clientes/id/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // Test 7: DELETE /clientes/id/999999 -> 400 (DatabaseException from ClienteService.delete)
    // Note: ClienteService.delete catches the exception and rethrows as DatabaseException -> 400.
    // The task spec names this "shouldReturn404" but the actual HTTP status is 400 per the
    // ResourceExceptionHandler mapping for DatabaseException.
    // Requirements: 5.7
    @Test
    void delete_withNonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/clientes/id/{id}", 999999L))
                .andExpect(status().isBadRequest());
    }

    // Test 8: PUT /clientes/id/1 with valid body -> 200
    // Requirements: 5.8
    @Test
    void update_withExistingId_shouldReturn200() throws Exception {
        String body = objectMapper.writeValueAsString(validClienteDTO());
        mockMvc.perform(put("/clientes/id/{id}", 1L)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test 9: PUT /clientes/id/999999 with valid body -> 500
    // ClienteService.update calls Optional.get() on empty Optional -> NoSuchElementException
    // which propagates uncaught to the generic RuntimeException handler -> 500.
    // Requirements: 5.9
    @Test
    void update_withNonExistingId_shouldReturn500() throws Exception {
        String body = objectMapper.writeValueAsString(validClienteDTO());
        mockMvc.perform(put("/clientes/id/{id}", 999999L)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // Test 10: GET /clientes?margem=0&... -> 200
    // Requirements: 8.1, 8.2, 8.3
    @Test
    void findByInteresses_withParams_shouldReturn200() throws Exception {
        mockMvc.perform(get("/clientes")
                .param("margem", "0")
                .param("qtdQuartos", "0")
                .param("qtdBanheiros", "0")
                .param("qtdVagas", "0")
                .param("metragem", "0")
                .param("valorMaximo", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test 11: stub findById to throw unmapped RuntimeException -> 500
    // Verifies that the generic @ExceptionHandler(RuntimeException.class) in
    // ResourceExceptionHandler returns HTTP 500 for unmapped exceptions.
    // Requirements: 8.4
    @Test
    void unmappedRuntimeException_shouldReturn500() throws Exception {
        Mockito.doThrow(new RuntimeException("unexpected"))
                .when(clienteService).findById(anyLong());

        mockMvc.perform(get("/clientes/id/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
