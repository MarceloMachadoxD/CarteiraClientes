package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDadosBasicosDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import com.github.marcelomachadoxd.carteiraclientes.repositories.UserRepository;
import com.github.marcelomachadoxd.carteiraclientes.repositories.VisitaRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class VisitaServiceTest {

    @InjectMocks
    private VisitaService visitaService;
    @Mock
    private VisitaRepository visitaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClienteRepository clienteRepository;

    private Long existingId, notExistId;
    private String existingNome, notExistNome;
    private PageImpl<Visita> page;
    private Pageable pageable;
    private Visita visita;
    private VisitaDTO visitaDTO;
    private Page<VisitaDTO> pageVisitaDTO;
    private User user;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        notExistId = 2L;

        visitaDTO = new VisitaDTO();
        visita = new Visita();
        visitaDTO.setId(existingId);
        existingNome = "Nome Válido";
        visita.setId(existingId);
        page = new PageImpl<>(List.of(visita));
        pageable = Mockito.mock(Pageable.class);

        user = new User();
        user.setId(existingId);
        user.setNome(existingNome);
        visitaDTO.setResponsavel(new UserDTO(user));
        visita.setResponsavel(user);

        cliente = new Cliente();
        existingNome = "Nome Válido";
        cliente.setId(existingId);
        cliente.setNome(existingNome);
        visitaDTO.setCliente(new ClienteDadosBasicosDTO(cliente));
        visita.setCliente(cliente);

        Mockito.when(visitaRepository.save(any())).thenReturn(visita);
        Mockito.when(visitaRepository.findById(existingId)).thenReturn(Optional.of(visita));
        Mockito.when(visitaRepository.findById(notExistId)).thenReturn(Optional.empty());
        Mockito.when(visitaRepository.findByResponsavelId(existingId, pageable)).thenReturn(page);
        Mockito.when(visitaRepository.findByClienteId(existingId, pageable)).thenReturn(page);
        Mockito.when(visitaRepository.findByClienteAndResponsavelId(existingId, existingId, pageable)).thenReturn(page);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(user));
        Mockito.when(clienteRepository.findById(any())).thenReturn(Optional.of(cliente));
    }

    @Test
    void findByIdShouldReturnVisita() {
        visitaDTO = visitaService.findById(existingId);
        assertEquals(visita.getId(), visitaDTO.getId());
    }

    @Test
    void findByNotExistedIdShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> visitaService.findById(notExistId));
    }

    @Test
    void findByResponsavelByIdShouldReturnPage() {
        pageVisitaDTO = visitaService.findResponsavelById(existingId, pageable);
        assertEquals(page.getContent().get(0).getId(), pageVisitaDTO.getContent().get(0).getId());
    }

    @Test
    void findByResponsavelByIdShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> visitaService.findResponsavelById(notExistId, pageable));
    }

    @Test
    void findByClienteByIdShouldReturnPage() {
        pageVisitaDTO = visitaService.findClientelById(existingId, pageable);
        assertFalse(page.getContent().isEmpty());
    }

    @Test
    void findByClienteByIdShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> visitaService.findClientelById(notExistId, pageable));
    }

    @Test
    void findbyClienteAndResponsavelId_ShouldReturnPage() {
        pageVisitaDTO = visitaService.findbyClienteAndResponsavelId(existingId, existingId, pageable);
        assertFalse(page.getContent().isEmpty());
    }

    @Test
    void findbyClienteAndResponsavelId_thatResponsavelNotExists_ShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> visitaService.findbyClienteAndResponsavelId(existingId, notExistId, pageable));
    }

    @Test
    void findbyClienteAndResponsavelId_thatClienteNotExists_ShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> visitaService.findbyClienteAndResponsavelId(notExistId, existingId, pageable));
    }

    @Test
    void findbyClienteAndResponsavelId_thatBothNotExists_ShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> visitaService.findbyClienteAndResponsavelId(notExistId, notExistId, pageable));
    }

    @Test
    void insertVisitaShouldReturnVisitaDTO() {
        VisitaDTO newVisitaDTO = visitaService.insert(visitaDTO);
        assertEquals(newVisitaDTO.getId(), visitaDTO.getId());
    }
}
