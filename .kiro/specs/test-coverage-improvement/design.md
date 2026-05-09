# Design Document — test-coverage-improvement

## Overview

This document describes the technical design for raising the CarteiraClientes test coverage to at least 70%, as measured by JaCoCo. The project currently has tests only in the service layer (`services/`). The design adds:

1. **JaCoCo plugin** configured in `pom.xml` with a minimum 70% line-coverage enforcement rule.
2. **Repository tests** (`@DataJpaTest`) for `ClienteRepository` and `VisitaRepository` custom JPQL queries.
3. **Service tests** (unit, `@ExtendWith(SpringExtension.class)`) filling the gaps in `ClienteService`, `UserService`, and `RoleService`.
4. **Resource tests** (`@SpringBootTest` + `MockMvc`) for all four controllers and the `ResourceExceptionHandler`.

The test profile already uses H2 in-memory with `data.sql` seed data, which provides a stable, reproducible dataset for integration-style tests.

---

## Architecture

The test suite follows the same layered architecture as the production code. Each layer is tested with the most appropriate strategy:

```
┌─────────────────────────────────────────────────────────────────┐
│  Resource Tests  (@SpringBootTest + MockMvc)                     │
│  ClienteResourceTest, VisitaResourceTest,                        │
│  UserResourceTest, RoleResourceTest                              │
├─────────────────────────────────────────────────────────────────┤
│  Service Tests  (@ExtendWith(SpringExtension.class) + Mockito)   │
│  ClienteServiceTest (gaps), UserServiceTest (gaps),              │
│  RoleServiceTest (new), VisitaServiceTest (existing)             │
├─────────────────────────────────────────────────────────────────┤
│  Repository Tests  (@DataJpaTest)                                │
│  ClienteRepositoryTest, VisitaRepositoryTest                     │
└─────────────────────────────────────────────────────────────────┘
```

**Key design decisions:**

- Resource tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `TestRestTemplate`, or `@AutoConfigureMockMvc` with `MockMvc`. Given the project has no security layer, `MockMvc` with `@SpringBootTest` is the simplest approach and avoids port conflicts.
- Repository tests use `@DataJpaTest`, which loads only the JPA slice (entities, repositories, H2) — fast and isolated.
- Service unit tests use `@ExtendWith(SpringExtension.class)` with `@InjectMocks` / `@Mock` (Mockito) — no Spring context, fastest execution.
- All tests that need seed data rely on the existing `data.sql` loaded via the `test` Spring profile.

---

## Components and Interfaces

### JaCoCo Plugin (`pom.xml`)

The `jacoco-maven-plugin` is added to the `<build><plugins>` section with two executions:

| Execution ID        | Goal              | Phase   | Purpose                                      |
|---------------------|-------------------|---------|----------------------------------------------|
| `jacoco-initialize` | `prepare-agent`   | default | Instruments bytecode for coverage collection |
| `jacoco-report`     | `report`          | test    | Generates HTML/XML report in `target/site/jacoco/` |
| `jacoco-check`      | `check`           | verify  | Fails build if line coverage < 70%           |

Exclusions applied to both `report` and `check`:
- `**/CarteiraClientesApplication.class`
- `**/config/SwaggerConfig.class`

### Test Class Inventory

#### Repository Layer

| Test Class                  | Annotation      | Covers                                      |
|-----------------------------|-----------------|---------------------------------------------|
| `ClienteRepositoryTest`     | `@DataJpaTest`  | `findByNome`, `findByInteresses`, `save`/`findById` |
| `VisitaRepositoryTest`      | `@DataJpaTest`  | `findByResponsavelId`, `findByClienteId`, `findByClienteAndResponsavelId` |

#### Service Layer (gap-filling)

| Test Class              | Annotation                          | Covers                                                  |
|-------------------------|-------------------------------------|---------------------------------------------------------|
| `ClienteServiceTest`    | `@ExtendWith(SpringExtension.class)`| `delete` (happy + error), `update` (happy + not-found), `findByInteresses` |
| `UserServiceTest`       | `@ExtendWith(SpringExtension.class)`| `delete` (happy + error), `findAllPageable`             |
| `RoleServiceTest`       | `@ExtendWith(SpringExtension.class)`| `findAll`, `insert`                                     |

> Note: `ClienteServiceTest` and `UserServiceTest` already exist. New test methods are added to the existing classes; `RoleServiceTest` is a new class.

#### Resource Layer

| Test Class              | Annotation                                    | Covers                                      |
|-------------------------|-----------------------------------------------|---------------------------------------------|
| `ClienteResourceTest`   | `@SpringBootTest` + `@AutoConfigureMockMvc`   | All `ClienteResource` endpoints             |
| `VisitaResourceTest`    | `@SpringBootTest` + `@AutoConfigureMockMvc`   | All `VisitaResource` endpoints              |
| `UserResourceTest`      | `@SpringBootTest` + `@AutoConfigureMockMvc`   | All `UserResource` endpoints                |
| `RoleResourceTest`      | `@SpringBootTest` + `@AutoConfigureMockMvc`   | All `RoleResource` endpoints                |

All resource tests are annotated with `@ActiveProfiles("test")` and `@Transactional` to roll back DB changes between tests.

---

## Data Models

### Test Data Strategy

**Seed data (`data.sql`)** is the primary source for read-only tests. The seed provides:
- 2 roles: `ADMIN` (id=1), `RESPONSAVEL` (id=2)
- 2 users: `marcelo` (id=1), `suelen` (id=2)
- 36 clients (id=1..36), including `Cliente` (id=1) with `nome='Cliente'`
- 31 visits, all with `responsavel_id=2` except the last one (`responsavel_id=1`)

**Programmatic setup** is used in `@DataJpaTest` tests and in resource tests that need to create/modify data. These tests are wrapped in `@Transactional` so changes are rolled back after each test method.

### Known Seed Data Facts Used in Tests

| Fact | Used by |
|------|---------|
| Client id=1 exists, nome='Cliente' | `ClienteResourceTest`, `ClienteRepositoryTest` |
| Client id=999999 does not exist | All "not found" scenarios |
| User id=2 is responsavel for visits id=1..30 | `VisitaRepositoryTest`, `VisitaResourceTest` |
| Visit id=1 exists, cliente_id=1, responsavel_id=2 | `VisitaResourceTest` |
| Role id=1 exists | `UserResourceTest` (POST /users with acessoId=1) |

### DTO Validation Constraints (relevant for 422 tests)

| DTO             | Field      | Constraint         | Triggers 422 when |
|-----------------|------------|--------------------|-------------------|
| `ClienteDTO`    | `email`    | `@Email`           | invalid email format |
| `ClienteDTO`    | numeric fields | `@DecimalMin("0")` | negative values |
| `UserInsertDTO` | `nome`     | `@NotBlank`        | blank or null |
| `UserInsertDTO` | `password` | `@NotBlank`        | blank or null |
| `UserInsertDTO` | `email`    | `@Email`           | invalid email format |
| `VisitaDTO`     | `dataVisita` | `@PastOrPresent` | future date |
| `VisitaDTO`     | `responsavel` | `@NotNull`      | null responsavel |
| `VisitaDTO`     | `cliente`  | `@NotNull`         | null cliente |

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

This feature is primarily about testing infrastructure and CRUD/query logic. Property-based testing (PBT) applies to the repository query logic, where the input space (client attributes, filter parameters, names) is large and varied inputs reveal edge cases in the JPQL queries. PBT does **not** apply to the resource layer (HTTP integration tests) or service layer (interaction tests with mocks), which are better served by example-based tests.

The PBT library chosen is **[jqwik](https://jqwik.net/)** (version 1.7.x), which integrates natively with JUnit 5 and is the most capable PBT library for Java. It is added as a `test`-scoped dependency.

---

### Property Reflection

Before finalizing properties, reviewing for redundancy:

- Requirements 2.1 and 2.2 (findByNome with match / no match) can be unified into one property: "for any name prefix, only clients whose names start with that prefix are returned."
- Requirements 2.3, 2.4, and 2.6 (findByInteresses inclusion/exclusion with and without margin) can be unified into one property about filter correctness.
- Requirements 2.5 (all-zeros wildcard) is a distinct invariant and stays separate.
- Requirements 2.7 (save/findById round-trip) is a classic round-trip property, stays separate.
- Requirements 3.1 and 3.2 (findByResponsavelId / findByClienteId) share the same structural pattern and can be expressed as one property about single-field filter correctness.
- Requirements 3.3, 3.4, 3.5 (findByClienteAndResponsavelId) form one property about combined-filter correctness.

After reflection: 5 distinct properties remain.

---

### Property 1: findByNome returns only prefix-matching clients (case-insensitive)

*For any* client name stored in the database, searching with any lowercase prefix of that name must return a page that includes that client, and every client in the result must have a name that starts with the search term (case-insensitively). Searching with a term that is not a prefix of any stored name must return an empty page.

**Validates: Requirements 2.1, 2.2**

---

### Property 2: findByInteresses filter correctness with margin

*For any* client with attributes `(qtdQuartos, qtdBanheiros, qtdVagas, metragem, valorMaximo)` and any filter parameters with a given `margem`, the client must appear in the results if and only if:
- `qtdQuartos == 0` OR `client.qtdQuartos >= qtdQuartos`
- `qtdBanheiros == 0` OR `client.qtdBanheiros >= qtdBanheiros`
- `qtdVagas == 0` OR `client.qtdVagas >= qtdVagas`
- `metragem == 0` OR `metragem >= client.metragem` (with margin tolerance on metragem)
- `valorMaximo == 0` OR `valorMaximo <= client.valorMaximo + client.valorMaximo * (margem/100)` (with margin tolerance on valorMaximo)

**Validates: Requirements 2.3, 2.4, 2.6**

---

### Property 3: findByInteresses with all-zero parameters returns all clients

*For any* set of clients in the database, calling `findByInteresses(0, 0, 0, 0, 0, 0, pageable)` must return a page whose `totalElements` equals the total number of clients in the database.

**Validates: Requirement 2.5**

---

### Property 4: Cliente save/findById round-trip preserves all fields

*For any* `Cliente` with valid field values (`nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`, `obs`), saving it via `clienteRepository.save()` and then retrieving it via `findById()` must return an `Optional` that is present and contains an entity with identical field values.

**Validates: Requirement 2.7**

---

### Property 5: Visita repository queries return only matching records

*For any* `Visita` persisted with a given `responsavel.id` and `cliente.id`:
- `findByResponsavelId(responsavel.id, pageable)` must return a page where every element has `responsavel.id` equal to the queried id.
- `findByClienteId(cliente.id, pageable)` must return a page where every element has `cliente.id` equal to the queried id.
- `findByClienteAndResponsavelId(cliente.id, responsavel.id, pageable)` must return a page that contains the persisted visit.
- Querying with an id that has no associated visits must return a page with `totalElements == 0`.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

---

## Error Handling

### Service Layer

| Scenario | Exception thrown | HTTP mapping |
|----------|-----------------|--------------|
| `findById` with non-existent id | `ResourceNotFoundException` | 404 |
| `delete` when repository throws | `DatabaseException` | 400 |
| `insert` when repository throws | `DatabaseException` | 400 |
| `update` with non-existent id | `NoSuchElementException` (uncaught — propagates as 500) | 500 |

> **Note:** `ClienteService.update` does not catch `NoSuchElementException` from `Optional.get()`. The requirement (4.4) documents this as the current behavior. The test must verify that `NoSuchElementException` is thrown; fixing this gap is out of scope for this feature.

> **Note:** `ResourceExceptionHandler` does not currently have a generic `RuntimeException` handler (Requirement 8.4). The test for this scenario requires either a dedicated test controller that throws an unmapped exception, or mocking a service bean to throw `RuntimeException`. The recommended approach is to use `@MockBean` in a `@SpringBootTest` test to stub a service method to throw `RuntimeException`, then verify the 500 response.

### Repository Layer

`@DataJpaTest` tests do not test exception handling — they test query correctness. Constraint violations (e.g., duplicate email) are not in scope for this feature.

### Resource Layer

All error scenarios are covered by the resource integration tests. The `ResourceExceptionHandler` is exercised indirectly through the full Spring context loaded by `@SpringBootTest`.

---

## Testing Strategy

### Test Framework Stack

| Concern | Tool |
|---------|------|
| Unit tests (service) | JUnit 5 + Mockito (`spring-boot-starter-test` already includes both) |
| Repository slice tests | `@DataJpaTest` (Spring Boot Test) |
| Resource integration tests | `@SpringBootTest` + `MockMvc` (`@AutoConfigureMockMvc`) |
| Property-based tests | **jqwik 1.7.x** (new `test`-scoped dependency) |
| JSON assertions | `MockMvcResultMatchers.jsonPath` (already available) |

### jqwik Dependency

Add to `pom.xml`:

```xml
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.7.4</version>
    <scope>test</scope>
</dependency>
```

### JaCoCo Plugin Configuration

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <id>jacoco-initialize</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>jacoco-report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
            <configuration>
                <excludes>
                    <exclude>**/CarteiraClientesApplication.class</exclude>
                    <exclude>**/config/SwaggerConfig.class</exclude>
                </excludes>
            </configuration>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <excludes>
                    <exclude>**/CarteiraClientesApplication.class</exclude>
                    <exclude>**/config/SwaggerConfig.class</exclude>
                </excludes>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Test Package Structure

```
src/test/java/.../carteiraclientes/
├── repositories/
│   ├── ClienteRepositoryTest.java      (@DataJpaTest)
│   └── VisitaRepositoryTest.java       (@DataJpaTest)
├── services/
│   ├── ClienteServiceTest.java         (existing — add gap methods)
│   ├── ClienteServiceTestIT.java       (existing — keep as-is)
│   ├── UserServiceTest.java            (existing — add gap methods)
│   ├── UserServiceTestIT.java          (existing — keep as-is)
│   ├── VisitaServiceTest.java          (existing — keep as-is)
│   └── RoleServiceTest.java            (new)
└── resources/
    ├── ClienteResourceTest.java        (@SpringBootTest + MockMvc)
    ├── VisitaResourceTest.java         (@SpringBootTest + MockMvc)
    ├── UserResourceTest.java           (@SpringBootTest + MockMvc)
    └── RoleResourceTest.java           (@SpringBootTest + MockMvc)
```

### Repository Tests (`@DataJpaTest`)

`@DataJpaTest` loads only the JPA slice: entities, repositories, and H2. It does **not** load `@Service` or `@RestController` beans. The `data.sql` seed is loaded automatically because `spring.sql.init.mode` defaults to `embedded` for H2.

```java
@DataJpaTest
@ActiveProfiles("test")
class ClienteRepositoryTest {
    @Autowired ClienteRepository clienteRepository;
    // jqwik @Property methods for Properties 1–4
    // JUnit @Test methods for edge cases
}
```

**Property test configuration:** Each `@Property` method in jqwik runs 100 tries by default. Tag each with a comment referencing the design property:
```java
// Feature: test-coverage-improvement, Property 1: findByNome returns only prefix-matching clients
@Property(tries = 100)
void findByNomePrefixMatchProperty(...) { ... }
```

### Service Tests (`@ExtendWith(SpringExtension.class)`)

Pure unit tests with Mockito. No Spring context is loaded. New methods are added to the existing `ClienteServiceTest` and `UserServiceTest` classes. `RoleServiceTest` is a new class following the same pattern.

```java
@ExtendWith(SpringExtension.class)
class RoleServiceTest {
    @InjectMocks RoleService roleService;
    @Mock RoleRepository roleRepository;
    // @Test methods for findAll and insert
}
```

### Resource Tests (`@SpringBootTest` + `MockMvc`)

Full Spring context with H2 and seed data. `@Transactional` ensures each test method rolls back its DB changes.

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteResourceTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    // @Test methods for each endpoint scenario
}
```

**MockMvc request pattern:**
```java
mockMvc.perform(get("/clientes/id/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id").value(1L));
```

**POST with body:**
```java
String body = objectMapper.writeValueAsString(clienteDTO);
mockMvc.perform(post("/clientes")
        .content(body)
        .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isCreated())
    .andExpect(header().exists("Location"));
```

### Handling Requirement 8.4 (Unmapped RuntimeException → 500)

The `ResourceExceptionHandler` does not have a `@ExceptionHandler(RuntimeException.class)` handler. To test this, use `@MockBean` to stub a service to throw an unmapped exception:

```java
@MockBean ClienteService clienteService;

@Test
void unmappedExceptionShouldReturn500() throws Exception {
    Mockito.when(clienteService.findById(anyLong()))
           .thenThrow(new RuntimeException("unexpected"));
    mockMvc.perform(get("/clientes/id/1"))
           .andExpect(status().isInternalServerError());
}
```

> **Important:** This test will fail unless a generic `RuntimeException` handler is added to `ResourceExceptionHandler`. The test documents the requirement; the implementation task must add the handler.

### Unit Test Balance

- **Unit tests (service):** Focus on interaction verification (was the right repository method called?) and exception mapping (does the service wrap exceptions correctly?). Avoid duplicating what integration tests already cover.
- **Integration tests (resource):** Cover the full HTTP contract — status codes, response body structure, header presence. One test per meaningful scenario (happy path, not-found, invalid input).
- **Property tests (repository):** Cover the query logic with varied inputs. Each property runs 100 iterations. Edge cases (empty results, zero parameters) are included in the property generators rather than as separate tests.

### Coverage Estimation

| Layer | Classes | Estimated coverage contribution |
|-------|---------|----------------------------------|
| `entities` | 4 | ~80% (constructors + getters exercised by other tests) |
| `dto` | 6 | ~85% (constructors + getters exercised by resource tests) |
| `repositories` | 4 | ~90% (all custom queries exercised by `@DataJpaTest`) |
| `services` | 4 | ~85% (all methods covered by unit + IT tests) |
| `resources` | 4 | ~90% (all endpoints covered by MockMvc tests) |
| `resources/exceptions` | 4 | ~80% (all handlers exercised by resource tests) |
| `services/exceptions` | 2 | ~100% (constructors exercised by service tests) |
| **Excluded** | `CarteiraClientesApplication`, `SwaggerConfig` | excluded from measurement |

Projected overall line coverage: **≥ 75%**, comfortably above the 70% threshold.
