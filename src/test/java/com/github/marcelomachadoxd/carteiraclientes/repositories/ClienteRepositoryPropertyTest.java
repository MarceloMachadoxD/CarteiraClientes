package com.github.marcelomachadoxd.carteiraclientes.repositories;

import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for ClienteRepository using jqwik.
 *
 * Uses ApplicationContextAware + TestContextManager pattern to ensure Spring
 * context is available both in JUnit (@Test) and jqwik (@Property) contexts.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ClienteRepositoryPropertyTest implements ApplicationContextAware {

    @Autowired
    private ClienteRepository clienteRepository;

    private static ClienteRepository sharedClienteRepository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        sharedClienteRepository = applicationContext.getBean(ClienteRepository.class);
    }

    private ClienteRepository getRepository() {
        if (clienteRepository != null) return clienteRepository;
        if (sharedClienteRepository != null) return sharedClienteRepository;
        try {
            TestContextManager tcm = new TestContextManager(ClienteRepositoryPropertyTest.class);
            tcm.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return clienteRepository;
    }

    // Feature: test-coverage-improvement, Property 1: findByNome returns only prefix-matching clients (case-insensitive)
    // Validates: Requirements 2.1, 2.2
    @Property(tries = 100)
    void findByNomePrefixMatchProperty(
            @ForAll @AlphaChars @StringLength(min = 1, max = 10) String prefix) {

        ClienteRepository repo = getRepository();

        // Save a client whose name starts with the prefix (uppercase first letter).
        // Use a unique suffix to avoid collisions across the 100 tries.
        String uniqueSuffix = "PBT" + System.nanoTime();
        String clientName = prefix.substring(0, 1).toUpperCase()
                + prefix.substring(1).toLowerCase()
                + uniqueSuffix;
        Cliente cliente = new Cliente();
        cliente.setNome(clientName);
        cliente.setEmail(prefix.toLowerCase() + System.nanoTime() + "@pbt.test");
        Cliente saved = repo.save(cliente);
        repo.flush();

        try {
            // Search using the lowercase prefix
            Pageable pageable = PageRequest.of(0, 100);
            Page<Cliente> result = repo.findByNome(prefix.toLowerCase(), pageable);

            // Property: every returned client's name must start with the prefix (case-insensitive)
            result.getContent().forEach(c ->
                    assertTrue(
                            c.getNome().toLowerCase().startsWith(prefix.toLowerCase()),
                            "Every result name should start with prefix '" + prefix
                                    + "' (case-insensitive), but got: " + c.getNome()
                    )
            );

            // The saved client must appear in the results
            boolean savedClientFound = result.getContent().stream()
                    .anyMatch(c -> c.getId().equals(saved.getId()));
            assertTrue(savedClientFound,
                    "Saved client with name '" + clientName
                            + "' should appear in results for prefix '" + prefix + "'");
        } finally {
            // Clean up to avoid data accumulation across tries
            repo.deleteById(saved.getId());
        }
    }
}
