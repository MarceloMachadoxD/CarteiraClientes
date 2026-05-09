package com.github.marcelomachadoxd.carteiraclientes.repositories;

import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContextManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ClienteRepositoryTest implements ApplicationContextAware {

    @Autowired
    private ClienteRepository clienteRepository;

    // Static reference used by jqwik @Property methods (which run on a separate instance
    // without Spring injection). Populated via ApplicationContextAware on the Spring-managed instance.
    private static ClienteRepository sharedClienteRepository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        sharedClienteRepository = applicationContext.getBean(ClienteRepository.class);
    }

    /**
     * Returns the ClienteRepository, initializing the Spring context via TestContextManager
     * if needed (e.g., when called from a jqwik @Property method on a non-Spring instance).
     */
    private ClienteRepository getRepository() {
        if (clienteRepository != null) {
            return clienteRepository;
        }
        if (sharedClienteRepository != null) {
            return sharedClienteRepository;
        }
        // Bootstrap Spring context for this instance (used by jqwik @Property methods)
        try {
            TestContextManager testContextManager = new TestContextManager(ClienteRepositoryTest.class);
            testContextManager.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return clienteRepository;
    }

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
    // Property 2: findByInteresses filter correctness with margin
    // Validates: Requirements 2.3, 2.4, 2.6
    // -------------------------------------------------------------------------

    @Property(tries = 50)
    void findByInteressesFilterCorrectnessProperty(
            @ForAll @IntRange(min = 1, max = 5) int qtdQuartos,
            @ForAll @IntRange(min = 1, max = 5) int qtdBanheiros,
            @ForAll @IntRange(min = 1, max = 5) int qtdVagas,
            @ForAll @IntRange(min = 1, max = 5) int metragem,
            @ForAll @IntRange(min = 1, max = 5) int valorMaximo) {

        ClienteRepository repo = getRepository();

        // Save a client whose attributes exactly match the filter parameters
        Cliente cliente = new Cliente();
        cliente.setNome("PropTest-" + qtdQuartos + "-" + qtdBanheiros);
        cliente.setEmail("proptest" + System.nanoTime() + "@test.com");
        cliente.setQtdQuartos(qtdQuartos);
        cliente.setQtdBanheiros(qtdBanheiros);
        cliente.setQtdVagas(qtdVagas);
        cliente.setMetragem(metragem);
        cliente.setValorMaximo(valorMaximo);
        Cliente saved = repo.save(cliente);
        repo.flush();

        // Search with the exact same values and margem=0
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Cliente> result = repo.findByInteresses(
                0, qtdQuartos, qtdBanheiros, qtdVagas, metragem, valorMaximo, pageable);

        // The saved client must appear in the result
        boolean found = result.getContent().stream()
                .anyMatch(c -> c.getId().equals(saved.getId()));
        assertTrue(found,
                "Client with qtdQuartos=" + qtdQuartos + ", qtdBanheiros=" + qtdBanheiros
                        + ", qtdVagas=" + qtdVagas + ", metragem=" + metragem
                        + ", valorMaximo=" + valorMaximo
                        + " should appear in findByInteresses result with exact match (margem=0)");
    }

    // -------------------------------------------------------------------------
    // Property 3: findByInteresses with all-zero parameters returns all clients
    // Validates: Requirement 2.5
    // -------------------------------------------------------------------------

    @Property(tries = 20)
    void findByInteressesAllZeroReturnsAllProperty(
            @ForAll @IntRange(min = 1, max = 5) int n) {

        ClienteRepository repo = getRepository();

        // Save N random clients with unique emails
        for (int i = 0; i < n; i++) {
            Cliente cliente = new Cliente();
            cliente.setNome("AllZeroProp-" + i);
            cliente.setEmail("allzero" + System.nanoTime() + i + "@test.com");
            cliente.setQtdQuartos(i + 1);
            cliente.setQtdBanheiros(i + 1);
            cliente.setQtdVagas(i + 1);
            cliente.setMetragem((i + 1) * 10);
            cliente.setValorMaximo((i + 1) * 100000);
            repo.save(cliente);
        }
        repo.flush();

        // Call findByInteresses with all-zero parameters (wildcard — matches everything)
        Page<Cliente> result = repo.findByInteresses(0, 0, 0, 0, 0, 0, PageRequest.of(0, 1000));

        assertTrue(result.getTotalElements() >= n,
                "findByInteresses with all-zero params should return at least the " + n
                        + " clients just saved, but got " + result.getTotalElements());
    }

    // -------------------------------------------------------------------------
    // Property 4: Cliente save/findById round-trip preserves all fields
    // Feature: test-coverage-improvement, Property 4: Cliente save/findById round-trip preserves all fields
    // Validates: Requirements 2.7
    // -------------------------------------------------------------------------

    @Property(tries = 50)
    void saveAndFindByIdRoundTripProperty(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String nome,
            @ForAll @IntRange(min = 0, max = 10) int qtdQuartos,
            @ForAll @IntRange(min = 0, max = 10) int qtdBanheiros,
            @ForAll @IntRange(min = 0, max = 10) int qtdVagas,
            @ForAll @IntRange(min = 0, max = 500) int metragem,
            @ForAll @IntRange(min = 0, max = 1000000) int valorMaximo) {

        ClienteRepository repo = getRepository();

        // Build a Cliente with the generated values; use System.nanoTime() to ensure unique email
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(nome + System.nanoTime() + "@test.com");
        cliente.setQtdQuartos(qtdQuartos);
        cliente.setQtdBanheiros(qtdBanheiros);
        cliente.setQtdVagas(qtdVagas);
        cliente.setMetragem(metragem);
        cliente.setValorMaximo(valorMaximo);

        Cliente saved = repo.save(cliente);
        repo.flush();

        Optional<Cliente> found = repo.findById(saved.getId());

        assertTrue(found.isPresent(), "findById should return a present Optional after save");
        Cliente retrieved = found.get();

        assertEquals(nome, retrieved.getNome(),
                "nome should be preserved after save/findById round-trip");
        assertEquals(Integer.valueOf(qtdQuartos), retrieved.getQtdQuartos(),
                "qtdQuartos should be preserved after save/findById round-trip");
        assertEquals(Integer.valueOf(qtdBanheiros), retrieved.getQtdBanheiros(),
                "qtdBanheiros should be preserved after save/findById round-trip");
        assertEquals(Integer.valueOf(qtdVagas), retrieved.getQtdVagas(),
                "qtdVagas should be preserved after save/findById round-trip");
        assertEquals(Integer.valueOf(metragem), retrieved.getMetragem(),
                "metragem should be preserved after save/findById round-trip");
        assertEquals(Integer.valueOf(valorMaximo), retrieved.getValorMaximo(),
                "valorMaximo should be preserved after save/findById round-trip");
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
