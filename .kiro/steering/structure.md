# Project Structure

## Package Root
`com.github.marcelomachadoxd.carteiraclientes`

## Layer Organization

```
src/main/java/.../carteiraclientes/
├── CarteiraClientesApplication.java   # Spring Boot entry point
├── config/                            # Spring configuration beans (Swagger, etc.)
├── entities/                          # JPA entities mapped to DB tables
├── dto/                               # Data Transfer Objects for API input/output
├── repositories/                      # Spring Data JPA interfaces
├── services/                          # Business logic layer
│   └── exceptions/                    # Service-level exceptions (ResourceNotFoundException, DatabaseException)
└── resources/                         # REST controllers (@RestController)
    └── exceptions/                    # Global exception handler + error response models

src/main/resources/
├── application.properties             # Base config (profile selection, JWT, OAuth2 placeholders)
├── application-test.properties        # H2 in-memory DB config for test/dev
├── data.sql                           # Seed data loaded on startup (test profile)
├── banner.txt                         # Custom Spring Boot banner
└── postman-requests/                  # Postman collection files for manual API testing

src/test/                              # JUnit tests
```

## Naming Conventions
- **Entities**: plain class name, e.g. `Cliente`, `Visita`, `User`, `Role`
- **DTOs**: `<Entity>DTO`, e.g. `ClienteDTO`, `VisitaDTO`. Specialized variants use a suffix: `ClienteDadosBasicosDTO`, `UserInsertDTO`
- **Repositories**: `<Entity>Repository` extending `JpaRepository<Entity, Long>`
- **Services**: `<Entity>Service` annotated with `@Service`
- **Resources (controllers)**: `<Entity>Resource` annotated with `@RestController`
- **DB tables**: prefixed with `tb_`, e.g. `tb_cliente`, `tb_visitas`, `tb_user`, `tb_role`

## Architecture Patterns
- Strict layered architecture: Resource → Service → Repository. Resources never access repositories directly.
- DTOs are used at the resource boundary; entities are never exposed directly in responses.
- DTOs include a copy constructor that accepts the entity: `new ClienteDTO(cliente)`.
- Services throw `ResourceNotFoundException` (→ 404) or `DatabaseException` (→ 400); `ResourceExceptionHandler` (`@ControllerAdvice`) maps these to structured JSON error responses.
- Bean validation (`@Valid`) is applied on request bodies in resources; validation errors are caught by `ResourceExceptionHandler` and returned as `ValidationError` with per-field messages.
- Repositories use JPQL `@Query` for custom queries (e.g. name search, interest-based filtering with margin tolerance).
- All list endpoints return paginated `Page<DTO>` using Spring Data's `Pageable`.
- `equals`/`hashCode` on entities are based solely on `id`.
- Field injection (`@Autowired`) is used throughout (constructor injection not currently adopted).
- Portuguese is used for domain field names and messages (e.g. `nome`, `email`, `qtdQuartos`, `responsavel`).
