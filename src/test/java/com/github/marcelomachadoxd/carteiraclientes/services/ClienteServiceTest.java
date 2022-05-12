package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
public class ClienteServiceTest {

    @InjectMocks
    private ClienteService clienteService;
    @Mock
    private ClienteRepository clienteRepository;

    private Long existingId, notExistId;
    private String existingNome, notExistNome;
    private PageImpl<Cliente> page;
    private Pageable pageable;
    private Cliente cliente;
    private ClienteDTO clienteDTO;
    private Page<ClienteDTO> pageClienteDTO;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        existingId = 1L;
        notExistId = 2L;
        existingNome = "Nome Válido";
        cliente.setId(existingId);
        cliente.setNome(existingNome);
        page = new PageImpl<>(List.of(cliente));
        pageable = Mockito.mock(Pageable.class);

        Mockito.when(clienteRepository.save(any())).thenReturn(cliente);
        Mockito.when(clienteRepository.findById(existingId)).thenReturn(Optional.of(cliente));
        Mockito.when(clienteRepository.findById(notExistId)).thenReturn(Optional.empty());
        Mockito.when(clienteRepository.findByNome(eq(existingNome), any())).thenReturn(page);
        Mockito.when(clienteRepository.findByNome(eq(notExistNome), any())).thenReturn(new PageImpl<>(List.of()));
    }

    @Test
    void findByNomeShouldReturnContent() {
        pageClienteDTO = clienteService.findClienteByNome(existingNome, pageable);
        assertEquals(pageClienteDTO.getContent().size(), 1);
        assertEquals(pageClienteDTO.getContent().get(0).getId(), existingId);
    }

    @Test
    void findByNotExistedNomeShouldNotReturnContent() {
        pageClienteDTO = clienteService.findClienteByNome(notExistNome, pageable);
        assertEquals(pageClienteDTO.getContent().size(), 0);
    }

    @Test
    void findByIdShouldReturnCliente() {
        clienteDTO = clienteService.findById(existingId);
        assertEquals(clienteDTO.getNome(), existingNome);
    }

    @Test
    void findByNotExistedIdShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> clienteService.findById(notExistId));
    }

    @Test
    void saveShouldReturnCliente() {
        ClienteDTO clienteDTOInsert = new ClienteDTO();
        clienteDTOInsert.setNome("Nome Válido");
        ClienteDTO clienteDTOresult = clienteService.insert(clienteDTOInsert);
        assertEquals(clienteDTOInsert.getNome(), clienteDTOresult.getNome());
    }
}
