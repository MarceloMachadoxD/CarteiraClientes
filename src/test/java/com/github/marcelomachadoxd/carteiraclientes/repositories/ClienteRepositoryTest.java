package com.github.marcelomachadoxd.carteiraclientes.repositories;

import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    // -------------------------------------------------------------------------
    // findByNome tests
    // -------------------------------------------------------------------------

    @Test
    void findByNome_withExistingPrefix_shouldReturnMatchingClients() {
        // Seed data has client id=1 with nome='Cliente'
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> result = clienteRepository.findByNome("cliente", pageable);

        assertTrue(result.getTotalElements() >= 1,
                "Expected at least one client matching prefix 'cliente'");
        result.getContent().forEach(c ->
                assertTrue(c.getNome().toLowerCase().startsWith("cliente"),
                        "Every result name should start with 'cliente' (case-insensitive), but got: " + c.getNome())
        );
    }

    @Test
    void findByNome_withNonMatchingTerm_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> result = clienteRepository.findByNome("zzznomeinexistente", pageable);

        assertEquals(0, result.getTotalElements(),
                "Expected empty page for non-matching search term");
    }

    // -------------------------------------------------------------------------
    // findByInteresses tests
    // -------------------------------------------------------------------------

    @Test
    void findByInteresses_withExactMatch_shouldIncludeClient() {
        // Save a client with known values
        Cliente cliente = new Cliente();
        cliente.setNome("TestInteresse");
        cliente.setEmail("testinteresse@test.com");
        cliente.setQtdQuartos(3);
        cliente.setQtdBanheiros(2);
        cliente.setQtdVagas(1);
        cliente.setMetragem(80);
        cliente.setValorMaximo(300000);
        Cliente saved = clienteRepository.save(cliente);

        Pageable pageable = PageRequest.of(0, 100);
        // Search with exact same values and margem=0
        Page<Cliente> result = clienteRepository.findByInteresses(
                0, 3, 2, 1, 80, 300000, pageable);

        boolean found = result.getContent().stream()
                .anyMatch(c -> c.getId().equals(saved.getId()));
        assertTrue(found, "Saved client should appear in findByInteresses result with exact match");
    }

    @Test
    void findByInteresses_withAllZeroParams_shouldReturnAllClients() {
        Pageable pageable = PageRequest.of(0, 100);
        long totalCount = clienteRepository.count();

        Page<Cliente> result = clienteRepository.findByInteresses(
                0, 0, 0, 0, 0, 0, pageable);

        assertEquals(totalCount, result.getTotalElements(),
                "findByInteresses with all-zero params should return all clients");
    }

    @Test
    void findByInteresses_withExcludingParams_shouldExcludeClient() {
        // Save a client with qtdQuartos=1
        Cliente cliente = new Cliente();
        cliente.setNome("TestExclude");
        cliente.setEmail("testexclude@test.com");
        cliente.setQtdQuartos(1);
        cliente.setQtdBanheiros(1);
        cliente.setQtdVagas(1);
        cliente.setMetragem(50);
        cliente.setValorMaximo(200000);
        Cliente saved = clienteRepository.save(cliente);

        Pageable pageable = PageRequest.of(0, 200);
        // Search with qtdQuartos=3 — client with qtdQuartos=1 should NOT appear
        Page<Cliente> result = clienteRepository.findByInteresses(
                0, 3, 0, 0, 0, 0, pageable);

        boolean found = result.getContent().stream()
                .anyMatch(c -> c.getId().equals(saved.getId()));
        assertFalse(found, "Client with qtdQuartos=1 should NOT appear when searching with qtdQuartos=3");
    }

    // -------------------------------------------------------------------------
    // save / findById round-trip test
    // -------------------------------------------------------------------------

    @Test
    void saveAndFindById_shouldPreserveAllFields() {
        Cliente cliente = new Cliente();
        cliente.setNome("RoundTrip Test");
        cliente.setEmail("roundtrip@test.com");
        cliente.setQtdQuartos(4);
        cliente.setQtdBanheiros(3);
        cliente.setQtdVagas(2);
        cliente.setMetragem(120);
        cliente.setValorMaximo(500000);
        cliente.setObs("Observação de teste");

        Cliente saved = clienteRepository.save(cliente);
        assertNotNull(saved.getId(), "Saved client should have a generated id");

        Optional<Cliente> found = clienteRepository.findById(saved.getId());

        assertTrue(found.isPresent(), "findById should return a present Optional");
        Cliente retrieved = found.get();

        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("RoundTrip Test", retrieved.getNome());
        assertEquals("roundtrip@test.com", retrieved.getEmail());
        assertEquals(Integer.valueOf(4), retrieved.getQtdQuartos());
        assertEquals(Integer.valueOf(3), retrieved.getQtdBanheiros());
        assertEquals(Integer.valueOf(2), retrieved.getQtdVagas());
        assertEquals(Integer.valueOf(120), retrieved.getMetragem());
        assertEquals(Integer.valueOf(500000), retrieved.getValorMaximo());
        assertEquals("Observação de teste", retrieved.getObs());
    }
}
