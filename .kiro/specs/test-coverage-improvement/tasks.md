# Implementation Plan: test-coverage-improvement

## Overview

Add JaCoCo coverage enforcement, jqwik property-based tests for repository queries, gap-filling service unit tests, and full MockMvc integration tests for all four resource controllers. The build must pass `mvnw.cmd verify` with ≥ 70% line coverage.

## Tasks

- [x] 1. Configure build infrastructure (pom.xml)
  - [x] 1.1 Add JaCoCo plugin to pom.xml
    - Add `jacoco-maven-plugin` version `0.8.8` inside `<build><plugins>`
    - Include three executions: `jacoco-initialize` (`prepare-agent`, default phase), `jacoco-report` (`report`, `test` phase), and `jacoco-check` (`check`, `verify` phase)
    - Apply exclusions `**/CarteiraClientesApplication.class` and `**/config/SwaggerConfig.class` to both `jacoco-report` and `jacoco-check`
    - Configure `jacoco-check` rule: `BUNDLE` element, `LINE` counter, `COVEREDRATIO` value, minimum `0.70`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 1.2 Add jqwik test dependency to pom.xml
    - Add `net.jqwik:jqwik:1.7.4` with `<scope>test</scope>` inside `<dependencies>`
    - _Requirements: 2.1, 2.3, 3.1_

- [x] 2. Repository tests — ClienteRepository
  - [x] 2.1 Create ClienteRepositoryTest with @DataJpaTest
    - Create `src/test/java/.../repositories/ClienteRepositoryTest.java`
    - Annotate with `@DataJpaTest` and `@ActiveProfiles("test")`
    - Inject `ClienteRepository` via `@Autowired`
    - Write `@Test findByNome_withExistingPrefix_shouldReturnMatchingClients`: call `findByNome("cliente", PageRequest.of(0,10))`, assert `totalElements >= 1` and every result name starts with "cliente" (case-insensitive) — uses seed client id=1 nome='Cliente'
    - Write `@Test findByNome_withNonMatchingTerm_shouldReturnEmptyPage`: call `findByNome("zzznomeinexistente", PageRequest.of(0,10))`, assert `totalElements == 0`
    - Write `@Test findByInteresses_withExactMatch_shouldIncludeClient`: save a `Cliente` with known values, call `findByInteresses(0, qtdQuartos, qtdBanheiros, qtdVagas, metragem, valorMaximo, pageable)`, assert the saved client is in the result
    - Write `@Test findByInteresses_withAllZeroParams_shouldReturnAllClients`: call `findByInteresses(0,0,0,0,0,0, PageRequest.of(0,100))`, assert `totalElements` equals `clienteRepository.count()`
    - Write `@Test findByInteresses_withExcludingParams_shouldExcludeClient`: save a `Cliente` with `qtdQuartos=1`, search with `qtdQuartos=3`, assert the saved client is NOT in the result
    - Write `@Test saveAndFindById_shouldPreserveAllFields`: save a fully-populated `Cliente`, call `findById(saved.getId())`, assert all fields match
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

  - [x] 2.2 Write property test for findByNome prefix matching (Property 1)
    - Add `@Property(tries = 100) void findByNomePrefixMatchProperty(...)` to `ClienteRepositoryTest`
    - Use jqwik `@ForAll @AlphaChars @StringLength(min=1, max=10) String prefix` as parameter
    - Save a `Cliente` whose name starts with the prefix (uppercase first letter), call `findByNome(prefix.toLowerCase(), PageRequest.of(0,100))`
    - Assert every returned client's name starts with the prefix (case-insensitive)
    - **Property 1: findByNome returns only prefix-matching clients (case-insensitive)**
    - **Validates: Requirements 2.1, 2.2**

  - [x] 2.3 Write property test for findByInteresses filter correctness (Property 2)
    - Add `@Property(tries = 50) void findByInteressesFilterCorrectnessProperty(...)` to `ClienteRepositoryTest`
    - Use jqwik `@ForAll @IntRange(min=1, max=5)` parameters for `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`
    - Save a `Cliente` with those exact values, call `findByInteresses(0, qtdQuartos, qtdBanheiros, qtdVagas, metragem, valorMaximo, PageRequest.of(0,100))`
    - Assert the saved client appears in the result
    - **Property 2: findByInteresses filter correctness with margin**
    - **Validates: Requirements 2.3, 2.4, 2.6**

  - [x] 2.4 Write property test for all-zero wildcard (Property 3)
    - Add `@Property(tries = 20) void findByInteressesAllZeroReturnsAllProperty(...)` to `ClienteRepositoryTest`
    - Save N random clients (N between 1 and 5), call `findByInteresses(0,0,0,0,0,0, PageRequest.of(0,1000))`
    - Assert `totalElements >= N` (seed data already present)
    - **Property 3: findByInteresses with all-zero parameters returns all clients**
    - **Validates: Requirement 2.5**

  - [x] 2.5 Write property test for save/findById round-trip (Property 4)
    - Add `@Property(tries = 50) void saveAndFindByIdRoundTripProperty(...)` to `ClienteRepositoryTest`
    - Use jqwik `@ForAll` parameters for `nome` (String), `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo` (non-negative integers)
    - Save a `Cliente` with those values, retrieve via `findById`, assert all fields are equal
    - **Property 4: Cliente save/findById round-trip preserves all fields**
    - **Validates: Requirement 2.7**

- [x] 3. Repository tests — VisitaRepository
  - [x] 3.1 Create VisitaRepositoryTest with @DataJpaTest
    - Create `src/test/java/.../repositories/VisitaRepositoryTest.java`
    - Annotate with `@DataJpaTest`, `@AutoConfigureTestDatabase(replace = Replace.NONE)`, and `@ActiveProfiles("test")`
    - **Apply the jqwik+Spring pattern** (see `.kiro/steering/testing.md`): implement `ApplicationContextAware`, keep static shared fields for `VisitaRepository`, `UserRepository`, `ClienteRepository`, and expose `getRepository()` helpers — required because task 3.2 adds `@Property` methods to this class
    - Inject `VisitaRepository`, `UserRepository`, `ClienteRepository` via `@Autowired`
    - Write `@Test findByResponsavelId_withExistingId_shouldReturnMatchingVisitas`: call `findByResponsavelId(2L, PageRequest.of(0,10))`, assert `totalElements >= 1` and every item has `responsavel.id == 2`
    - Write `@Test findByClienteId_withExistingId_shouldReturnMatchingVisitas`: call `findByClienteId(1L, PageRequest.of(0,10))`, assert `totalElements >= 1` and every item has `cliente.id == 1`
    - Write `@Test findByClienteAndResponsavelId_withExistingCombination_shouldReturnVisita`: call `findByClienteAndResponsavelId(1L, 2L, PageRequest.of(0,10))`, assert `totalElements >= 1`
    - Write `@Test findByResponsavelId_withNonExistingId_shouldReturnEmptyPage`: call `findByResponsavelId(Long.MAX_VALUE, PageRequest.of(0,10))`, assert `totalElements == 0`
    - Write `@Test findByClienteAndResponsavelId_withNonExistingCombination_shouldReturnEmptyPage`: call `findByClienteAndResponsavelId(Long.MAX_VALUE, Long.MAX_VALUE, PageRequest.of(0,10))`, assert `totalElements == 0`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 3.2 Write property test for Visita repository queries (Property 5)
    - Add `@Property(tries = 30) void visitaRepositoryQueriesReturnOnlyMatchingRecordsProperty(...)` to `VisitaRepositoryTest`
    - Programmatically save a `Visita` with a known `responsavel` (user id=1) and `cliente` (id=1)
    - Assert `findByResponsavelId(1L, ...)` returns a page where every element has `responsavel.id == 1`
    - Assert `findByClienteId(1L, ...)` returns a page where every element has `cliente.id == 1`
    - Assert `findByClienteAndResponsavelId(1L, 1L, ...)` returns `totalElements >= 1`
    - Assert `findByResponsavelId(Long.MAX_VALUE, ...)` returns `totalElements == 0`
    - **Property 5: Visita repository queries return only matching records**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

- [x] 4. Checkpoint — repository layer
  - Ensure all repository tests compile and pass with `mvnw.cmd test -pl . -Dtest="ClienteRepositoryTest,VisitaRepositoryTest"`. Ask the user if any issues arise.

- [x] 5. Service tests — gap-filling for ClienteService
  - [x] 5.1 Add missing test methods to ClienteServiceTest
    - Open existing `src/test/java/.../services/ClienteServiceTest.java`
    - Add `@BeforeEach` stub: `Mockito.doNothing().when(clienteRepository).deleteById(existingId)` and `Mockito.doThrow(RuntimeException.class).when(clienteRepository).deleteById(notExistId)`
    - Add `@Test deleteShouldCallDeleteById`: call `clienteService.delete(existingId)`, verify `clienteRepository.deleteById(existingId)` was invoked once
    - Add `@Test deleteWithNonExistingIdShouldThrowDatabaseException`: assert `DatabaseException` is thrown when calling `clienteService.delete(notExistId)`
    - Add `@Test updateShouldCallSave`: set up `clienteRepository.findById(existingId)` returning `Optional.of(cliente)`, call `clienteService.update(existingId, clienteDTO)`, verify `clienteRepository.save(any())` was invoked
    - Add `@Test updateWithNonExistingIdShouldThrowNoSuchElementException`: set up `clienteRepository.findById(notExistId)` returning `Optional.empty()`, assert `NoSuchElementException` is thrown
    - Add `@Test findByInteressesShouldReturnPage`: stub `clienteRepository.findByInteresses(any(), any(), any(), any(), any(), any(), any())` to return `page`, call `clienteService.findByInteresses(0,2,1,1,60,200000,pageable)`, assert result is not null
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 6. Service tests — gap-filling for UserService
  - [x] 6.1 Add missing test methods to UserServiceTest
    - Open existing `src/test/java/.../services/UserServiceTest.java`
    - Add `@BeforeEach` stub: `Mockito.doNothing().when(userRepository).deleteById(existingId)` and `Mockito.doThrow(RuntimeException.class).when(userRepository).deleteById(notExistId)`
    - Add stub for `userRepository.findAllPageable(any())` returning a `PageImpl<User>` with one user
    - Add `@Test deleteShouldCallDeleteById`: call `userService.delete(existingId)`, verify `userRepository.deleteById(existingId)` was invoked once
    - Add `@Test deleteWithNonExistingIdShouldThrowDatabaseException`: assert `DatabaseException` is thrown when calling `userService.delete(notExistId)`
    - Add `@Test findAllPageableShouldReturnPage`: call `userService.findAllPageable(pageable)`, assert result is not null and `totalElements >= 1`
    - _Requirements: 4.6, 4.7, 4.8_

- [x] 7. Service tests — new RoleServiceTest
  - [x] 7.1 Create RoleServiceTest
    - Create `src/test/java/.../services/RoleServiceTest.java`
    - Annotate with `@ExtendWith(SpringExtension.class)`
    - Declare `@InjectMocks RoleService roleService` and `@Mock RoleRepository roleRepository`
    - In `@BeforeEach`: stub `roleRepository.findAll()` to return `List.of(new Role(1L, "ROLE_ADMIN"))` and `roleRepository.save(any())` to return `new Role(1L, "ROLE_TEST")`
    - Add `@Test findAllShouldReturnList`: call `roleService.findAll()`, assert result is not null and size is 1
    - Add `@Test insertShouldCallSaveAndReturnRoleDTO`: create `RoleDTO` with `nome="ROLE_TEST"`, call `roleService.insert(roleDTO)`, verify `roleRepository.save(any())` was invoked, assert returned `RoleDTO.getNome()` equals `"ROLE_TEST"`
    - _Requirements: 4.9, 4.10_

- [x] 8. Checkpoint — service layer
  - Ensure all service tests pass with `mvnw.cmd test -pl . -Dtest="ClienteServiceTest,UserServiceTest,RoleServiceTest"`. Ask the user if any issues arise.

- [x] 9. Add generic RuntimeException handler to ResourceExceptionHandler
  - Open `src/main/java/.../resources/exceptions/ResourceExceptionHandler.java`
  - Add a new `@ExceptionHandler(RuntimeException.class)` method that returns HTTP 500 with a `StandardError` body containing `timestamp`, `status=500`, `error="Internal Server Error"`, `message=e.getMessage()`, and `path=request.getRequestURI()`
  - This handler must be ordered after the specific handlers so it only catches unmapped exceptions
  - _Requirements: 8.4_

- [x] 10. Resource tests — ClienteResource
  - [x] 10.1 Create ClienteResourceTest
    - Create `src/test/java/.../resources/ClienteResourceTest.java`
    - Annotate with `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@Transactional`
    - Inject `MockMvc` and `ObjectMapper` via `@Autowired`
    - Add `@Test findById_withExistingId_shouldReturn200`: `GET /clientes/id/1`, assert status 200 and `$.id == 1`
    - Add `@Test findById_withNonExistingId_shouldReturn404`: `GET /clientes/id/999999`, assert status 404
    - Add `@Test findByNome_withExistingNome_shouldReturn200WithResults`: `GET /clientes/nome/Cliente`, assert status 200 and `$.totalElements >= 1`
    - Add `@Test insert_withValidBody_shouldReturn201WithLocation`: `POST /clientes` with valid `ClienteDTO` JSON, assert status 201 and `Location` header exists
    - Add `@Test insert_withInvalidEmail_shouldReturn422WithErrors`: `POST /clientes` with `email="not-an-email"`, assert status 422 and `$.errors[0].fieldName` exists
    - Add `@Test delete_withExistingId_shouldReturn204`: `DELETE /clientes/id/1`, assert status 204
    - Add `@Test delete_withNonExistingId_shouldReturn404`: `DELETE /clientes/id/999999`, assert status 404
    - Add `@Test update_withExistingId_shouldReturn200`: `PUT /clientes/id/1` with valid body, assert status 200
    - Add `@Test update_withNonExistingId_shouldReturn404`: `PUT /clientes/id/999999` with valid body, assert status 404 (or 500 per current behavior — document actual status)
    - Add `@Test findByInteresses_withParams_shouldReturn200`: `GET /clientes?margem=0&qtdQuartos=0&qtdBanheiros=0&qtdVagas=0&metragem=0&valorMaximo=0`, assert status 200
    - Add `@Test unmappedRuntimeException_shouldReturn500`: use `@MockBean ClienteService` to stub `findById(anyLong())` throwing `new RuntimeException("unexpected")`, `GET /clientes/id/1`, assert status 500
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 8.1, 8.2, 8.3, 8.4_

- [x] 11. Resource tests — VisitaResource
  - [x] 11.1 Create VisitaResourceTest
    - Create `src/test/java/.../resources/VisitaResourceTest.java`
    - Annotate with `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@Transactional`
    - Inject `MockMvc` and `ObjectMapper` via `@Autowired`
    - Add `@Test findById_withExistingId_shouldReturn200`: `GET /visitas/1`, assert status 200 and `$.id == 1`
    - Add `@Test findById_withNonExistingId_shouldReturn404`: `GET /visitas/999999`, assert status 404
    - Add `@Test findByResponsavelId_withExistingId_shouldReturn200`: `GET /visitas/responsavel/2`, assert status 200 and `$.totalElements >= 1`
    - Add `@Test findByClienteId_withExistingId_shouldReturn200`: `GET /visitas/cliente/1`, assert status 200 and `$.totalElements >= 1`
    - Add `@Test insert_withValidBody_shouldReturn200`: `POST /visitas` with JSON `{"dataVisita":"<past ISO instant>","responsavel":{"id":2},"cliente":{"id":1}}`, assert status 200 and `$.id` is not null
    - Add `@Test insert_withNullDataVisita_shouldReturn422`: `POST /visitas` with body missing `dataVisita`, assert status 422
    - Add `@Test insert_withNonExistingResponsavel_shouldReturn404`: `POST /visitas` with `responsavel.id=999999`, assert status 404
    - Add `@Test delete_withExistingId_shouldReturn204`: `DELETE /visitas/1`, assert status 204
    - Add `@Test delete_withNonExistingId_shouldReturn404`: `DELETE /visitas/999999`, assert status 404
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8_

- [x] 12. Resource tests — UserResource and RoleResource
  - [x] 12.1 Create UserResourceTest
    - Create `src/test/java/.../resources/UserResourceTest.java`
    - Annotate with `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@Transactional`
    - Inject `MockMvc` and `ObjectMapper` via `@Autowired`
    - Add `@Test findAllPageable_shouldReturn200WithPage`: `GET /users`, assert status 200 and `$.content` is an array
    - Add `@Test findById_withExistingId_shouldReturn200`: `GET /users/1`, assert status 200 and `$.id == 1`
    - Add `@Test findById_withNonExistingId_shouldReturn404`: `GET /users/999999`, assert status 404
    - Add `@Test insert_withValidBody_shouldReturn200`: `POST /users` with JSON `{"nome":"Test","email":"test@test.com","password":"123456","acessoId":1}`, assert status 200 and `$.nome == "Test"`
    - Add `@Test insert_withMissingFields_shouldReturn422`: `POST /users` with empty JSON `{}`, assert status 422
    - Add `@Test delete_withExistingId_shouldReturn204`: `DELETE /users/1`, assert status 204
    - Add `@Test delete_withNonExistingId_shouldReturn400`: `DELETE /users/999999`, assert status 400
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.8, 7.9_

  - [x] 12.2 Create RoleResourceTest
    - Create `src/test/java/.../resources/RoleResourceTest.java`
    - Annotate with `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@Transactional`
    - Inject `MockMvc` and `ObjectMapper` via `@Autowired`
    - Add `@Test findAll_shouldReturn200WithList`: `GET /roles`, assert status 200 and response is a JSON array with at least one element
    - Add `@Test insert_withValidBody_shouldReturn201`: `POST /roles` with JSON `{"nome":"ROLE_TEST"}`, assert status 201 and `Location` header exists
    - _Requirements: 7.6, 7.7_

- [x] 13. Final checkpoint — full build verification
  - Run `mvnw.cmd test` and confirm all tests pass
  - Run `mvnw.cmd verify` and confirm the JaCoCo check passes (≥ 70% line coverage)
  - If coverage is below 70%, identify which classes are under-covered from `target/site/jacoco/index.html` and add targeted tests
  - Ask the user if any issues arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP; the mandatory tests alone should be sufficient to reach 70% coverage
- Property tests (2.2–2.5, 3.2) use jqwik `@Property` annotation — they run alongside JUnit 5 tests automatically because jqwik integrates with the JUnit Platform
- **⚠️ jqwik + Spring integration:** jqwik creates its own test instance separate from Spring's, so `@Autowired` fields are `null` inside `@Property` methods. Every `@DataJpaTest` class with `@Property` methods MUST implement `ApplicationContextAware`, keep a static shared repository field, and expose a `getRepository()` helper — see `ClienteRepositoryTest.java` as the reference implementation and `.kiro/steering/testing.md` for the full pattern. This was the root cause of failures in tasks 2.1 and 2.2.
- The `@Transactional` annotation on resource tests ensures each test method rolls back DB changes, keeping tests independent
- `ClienteService.update` with a non-existing id throws `NoSuchElementException` (not caught by the service) — the resource test should document the actual HTTP status returned (likely 500 via the new generic handler added in task 9)
- Seed data facts used across tests: client id=1 nome='Cliente', user id=2 is responsavel for visits 1–30, visit id=1 has cliente_id=1 and responsavel_id=2, role id=1 exists
- All resource tests use `@ActiveProfiles("test")` to activate H2 in-memory DB via `application-test.properties`
- Use `mvnw.cmd` (Windows) for all Maven commands

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["2.1", "3.1"] },
    { "id": 2, "tasks": ["2.2", "2.3", "2.4", "2.5", "3.2"] },
    { "id": 3, "tasks": ["5.1", "6.1", "7.1"] },
    { "id": 4, "tasks": ["9"] },
    { "id": 5, "tasks": ["10.1", "11.1", "12.1", "12.2"] }
  ]
}
```
