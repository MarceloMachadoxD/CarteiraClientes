package com.github.marcelomachadoxd.carteiraclientes.repositories;

import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContextManager;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class VisitaRepositoryTest implements ApplicationContextAware {

    @Autowired
    private VisitaRepository visitaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Static references used by jqwik @Property methods (which run on a separate instance
    // without Spring injection). Populated via ApplicationContextAware on the Spring-managed instance.
    private static VisitaRepository sharedVisitaRepository;
    private static UserRepository sharedUserRepository;
    private static ClienteRepository sharedClienteRepository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        sharedVisitaRepository = applicationContext.getBean(VisitaRepository.class);
        sharedUserRepository = applicationContext.getBean(UserRepository.class);
        sharedClienteRepository = applicationContext.getBean(ClienteRepository.class);
    }

    /**
     * Returns the VisitaRepository, initializing the Spring context via TestContextManager
     * if needed (e.g., when called from a jqwik @Property method on a non-Spring instance).
     */
    private VisitaRepository getVisitaRepository() {
        if (visitaRepository != null) {
            return visitaRepository;
        }
        if (sharedVisitaRepository != null) {
            return sharedVisitaRepository;
        }
        try {
            TestContextManager testContextManager = new TestContextManager(VisitaRepositoryTest.class);
            testContextManager.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return visitaRepository;
    }

    /**
     * Returns the UserRepository, initializing the Spring context via TestContextManager if needed.
     */
    private UserRepository getUserRepository() {
        if (userRepository != null) {
            return userRepository;
        }
        if (sharedUserRepository != null) {
            return sharedUserRepository;
        }
        try {
            TestContextManager testContextManager = new TestContextManager(VisitaRepositoryTest.class);
            testContextManager.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return userRepository;
    }

    /**
     * Returns the ClienteRepository, initializing the Spring context via TestContextManager if needed.
     */
    private ClienteRepository getClienteRepository() {
        if (clienteRepository != null) {
            return clienteRepository;
        }
        if (sharedClienteRepository != null) {
            return sharedClienteRepository;
        }
        try {
            TestContextManager testContextManager = new TestContextManager(VisitaRepositoryTest.class);
            testContextManager.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return clienteRepository;
    }

    // -------------------------------------------------------------------------
    // findByResponsavelId tests
    // -------------------------------------------------------------------------

    @Test
    void findByResponsavelId_withExistingId_shouldReturnMatchingVisitas() {
        // Seed data: user id=2 is responsavel for visits id=1..30
        Pageable pageable = PageRequest.of(0, 10);
        Page<Visita> result = visitaRepository.findByResponsavelId(2L, pageable);

        assertTrue(result.getTotalElements() >= 1,
                "Expected at least one visita for responsavel id=2");
        result.getContent().forEach(v ->
                assertEquals(2L, v.getResponsavel().getId(),
                        "Every result should have responsavel.id == 2, but got: " + v.getResponsavel().getId())
        );
    }

    @Test
    void findByResponsavelId_withNonExistingId_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Visita> result = visitaRepository.findByResponsavelId(Long.MAX_VALUE, pageable);

        assertEquals(0, result.getTotalElements(),
                "Expected empty page for non-existing responsavel id");
    }

    // -------------------------------------------------------------------------
    // findByClienteId tests
    // -------------------------------------------------------------------------

    @Test
    void findByClienteId_withExistingId_shouldReturnMatchingVisitas() {
        // Seed data: visit id=1 has cliente_id=1
        Pageable pageable = PageRequest.of(0, 10);
        Page<Visita> result = visitaRepository.findByClienteId(1L, pageable);

        assertTrue(result.getTotalElements() >= 1,
                "Expected at least one visita for cliente id=1");
        result.getContent().forEach(v ->
                assertEquals(1L, v.getCliente().getId(),
                        "Every result should have cliente.id == 1, but got: " + v.getCliente().getId())
        );
    }

    // -------------------------------------------------------------------------
    // findByClienteAndResponsavelId tests
    // -------------------------------------------------------------------------

    @Test
    void findByClienteAndResponsavelId_withExistingCombination_shouldReturnVisita() {
        // Seed data: visit id=1 has cliente_id=1 and responsavel_id=2
        Pageable pageable = PageRequest.of(0, 10);
        Page<Visita> result = visitaRepository.findByClienteAndResponsavelId(1L, 2L, pageable);

        assertTrue(result.getTotalElements() >= 1,
                "Expected at least one visita for cliente id=1 and responsavel id=2");
    }

    @Test
    void findByClienteAndResponsavelId_withNonExistingCombination_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Visita> result = visitaRepository.findByClienteAndResponsavelId(
                Long.MAX_VALUE, Long.MAX_VALUE, pageable);

        assertEquals(0, result.getTotalElements(),
                "Expected empty page for non-existing cliente/responsavel combination");
    }

    // Feature: test-coverage-improvement, Property 5: Visita repository queries return only matching records
    // Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5
    @Property(tries = 30)
    void visitaRepositoryQueriesReturnOnlyMatchingRecordsProperty() {
        VisitaRepository visitaRepo = getVisitaRepository();
        UserRepository userRepo = getUserRepository();
        ClienteRepository clienteRepo = getClienteRepository();

        // Load existing user (id=1) and cliente (id=1) from seed data
        User user = userRepo.findById(1L).orElseThrow(() ->
                new RuntimeException("User id=1 not found in seed data"));
        Cliente cliente = clienteRepo.findById(1L).orElseThrow(() ->
                new RuntimeException("Cliente id=1 not found in seed data"));

        // Create and save a new Visita with responsavel=user(id=1), cliente=cliente(id=1)
        Visita visita = new Visita();
        visita.setResponsavel(user);
        visita.setCliente(cliente);
        visita.setDataVisita(Instant.now().minusSeconds(3600));
        visitaRepo.save(visita);
        visitaRepo.flush();

        // Assert findByResponsavelId(1L) returns only visitas where responsavel.id == 1
        Page<Visita> byResponsavel = visitaRepo.findByResponsavelId(1L, PageRequest.of(0, 100));
        byResponsavel.getContent().forEach(v ->
                assertEquals(1L, v.getResponsavel().getId(),
                        "Every result of findByResponsavelId(1) must have responsavel.id == 1"));

        // Assert findByClienteId(1L) returns only visitas where cliente.id == 1
        Page<Visita> byCliente = visitaRepo.findByClienteId(1L, PageRequest.of(0, 100));
        byCliente.getContent().forEach(v ->
                assertEquals(1L, v.getCliente().getId(),
                        "Every result of findByClienteId(1) must have cliente.id == 1"));

        // Assert findByClienteAndResponsavelId(1L, 1L) returns at least 1 result
        Page<Visita> byClienteAndResponsavel = visitaRepo.findByClienteAndResponsavelId(
                1L, 1L, PageRequest.of(0, 100));
        assertTrue(byClienteAndResponsavel.getTotalElements() >= 1,
                "findByClienteAndResponsavelId(1,1) must return at least 1 result");

        // Assert findByResponsavelId(Long.MAX_VALUE) returns 0 results
        Page<Visita> byNonExistingResponsavel = visitaRepo.findByResponsavelId(
                Long.MAX_VALUE, PageRequest.of(0, 10));
        assertEquals(0, byNonExistingResponsavel.getTotalElements(),
                "findByResponsavelId(Long.MAX_VALUE) must return 0 results");
    }
}
