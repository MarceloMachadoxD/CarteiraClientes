package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.transaction.Transactional;
import java.util.Optional;

@SpringBootTest
@Transactional
class ClienteServiceTestIT {
    Cliente cliente;

    PageRequest pageRequest;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    private ClienteService clienteService;

    @BeforeEach
    void Setup() {
        cliente = new Cliente();
        cliente.setNome("Cliente");
        cliente.setId(1L);
        pageRequest = PageRequest.of(0, 10);
    }


    @Test
    void findClienteByNome() {
        Page<ClienteDTO> clienteResultado = clienteService
            .findClienteByNome("Cliente", pageRequest);
        ClienteDTO clienteResultadoEsperado = clienteResultado.getContent().get(0);
        Assertions.assertEquals(clienteResultadoEsperado.getId(), cliente.getId());
    }

    @Test
    void findById() {
        ClienteDTO clienteResultado = clienteService
            .findById(1L);
        Assertions.assertEquals(clienteResultado.getNome(), cliente.getNome());
    }

    @Test
    void insertCliente() {
        ClienteDTO clienteDTO = new ClienteDTO(cliente);
        ClienteDTO resultado = clienteService.insert(clienteDTO);
        Assertions.assertEquals(resultado.getNome(), cliente.getNome());
    }

    @Test
    void deleteCliente() {
        clienteService.delete(1L);
        Assertions.assertEquals(clienteRepository.findById(1L), Optional.empty());
    }

    @Test
    void updateCliente() {
        ClienteDTO clienteDTO = new ClienteDTO(cliente);
        clienteDTO.setNome("Cliente Atualizado");
        clienteService.update(1L, clienteDTO);
        Assertions.assertEquals("Cliente Atualizado", clienteRepository.findById(1L).get().getNome());
    }
}