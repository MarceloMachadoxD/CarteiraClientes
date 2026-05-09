# Tech Stack

## Core
- **Java 11**
- **Spring Boot 2.4.5** (parent), with Spring Boot 3.0.0 starters for web, data-jpa, validation, and test
- **Spring Data JPA** with Hibernate as the JPA provider
- **H2** in-memory database (test/dev profile)
- **Springfox Swagger 2.9.2** for API documentation

## Build System
- **Maven** (wrapper included: `mvnw` / `mvnw.cmd`)
- Group ID: `com.github.marcelomachadoxd`
- Artifact ID: `CarteiraClientes`

## Configuration Profiles
- `test` (default) — uses H2 in-memory DB, H2 console enabled at `/h2-console`, SQL logging on
- Production profile would require external DB and real OAuth2/JWT secrets via environment variables:
  - `APP_PROFILE`, `CLIENT_ID`, `CLIENT_SECRET`, `JWT_SECRET`, `JWT_DURATION`

## Common Commands

```bash
# Run the application (test profile active by default)
./mvnw spring-boot:run

# Build (skip tests)
./mvnw clean package -DskipTests

# Run tests
./mvnw test

# Windows
mvnw.cmd spring-boot:run
mvnw.cmd clean package -DskipTests
mvnw.cmd test
```

## Notes
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console (test profile): `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`, no password)
- Postman collections are in `src/main/resources/postman-requests/`
- Database seed data is in `src/main/resources/data.sql`
