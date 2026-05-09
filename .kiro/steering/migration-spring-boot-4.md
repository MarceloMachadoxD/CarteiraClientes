---
inclusion: always
---

# Migração Concluída: Spring Boot 4 + Java 25

A migração de Java 11 + Spring Boot 2.x/3.x para **Java 25 + Spring Boot 4.0.0** está concluída. As regras abaixo documentam decisões permanentes e padrões que devem ser mantidos em todo código novo ou modificado.

---

## Regras Permanentes de Código

### Imports obrigatórios (Jakarta EE)

Nunca usar `javax.*`. Sempre usar os equivalentes `jakarta.*`:

| Substituído | Usar |
|---|---|
| `javax.persistence.*` | `jakarta.persistence.*` |
| `javax.validation.constraints.*` | `jakarta.validation.constraints.*` |
| `javax.validation.Valid` | `jakarta.validation.Valid` |
| `javax.servlet.http.HttpServletRequest` | `jakarta.servlet.http.HttpServletRequest` |

### Jackson 3 (ObjectMapper)

Spring Boot 4 usa Jackson 3 como serializador padrão. Em testes e código de produção, usar sempre:

```java
import tools.jackson.databind.ObjectMapper; // correto
// import com.fasterxml.jackson.databind.ObjectMapper; // ERRADO — não é bean Spring
```

### Anotações de teste Mockito

| Substituído | Usar |
|---|---|
| `@SpyBean` (pacote `org.springframework.boot.test.mock.mockito`) | `@MockitoSpyBean` (pacote `org.springframework.test.context.bean.override.mockito`) |
| `@ExtendWith(SpringExtension.class)` em testes unitários com `@InjectMocks` | `@ExtendWith(MockitoExtension.class)` + `@MockitoSettings(strictness = Strictness.LENIENT)` |

### Imports de test autoconfigure

Spring Boot 4 reorganizou os módulos de test. Usar sempre:

```java
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
```

As dependências `spring-boot-starter-data-jpa-test` e `spring-boot-starter-webmvc-test` já estão no `pom.xml`.

---

## Padrões de Teste

### Testes unitários de service

Usar `@ExtendWith(MockitoExtension.class)` — nunca `SpringExtension` para testes sem contexto Spring.

### Testes de repositório com jqwik (`@Property`)

jqwik 1.9.3 não injeta beans Spring automaticamente em métodos `@Property`. Padrão obrigatório:

- Implementar `ApplicationContextAware` na classe de teste
- Armazenar o bean em campo estático
- Expor via método `getRepository()` com fallback via `TestContextManager`

### Testes de resource/controller com jqwik (`@Property`)

Mesmo padrão acima. Expor `getMockMvc()`, `getObjectMapper()` e `getClienteRepository()` via campos estáticos + `ApplicationContextAware`.

### `@DataJpaTest` com `data.sql`

`data.sql` é executado antes do DDL do Hibernate no Spring Boot 4. A propriedade abaixo já está configurada e **não deve ser removida**:

```properties
# application-test.properties
spring.jpa.defer-datasource-initialization=true
```

---

## Padrões de Service (delete)

`deleteById` com ID inexistente **não lança exceção** no Spring Data JPA com Spring Boot 4. Todo método `delete` em services deve verificar existência antes de deletar:

```java
// Padrão obrigatório em ClienteService, UserService, VisitaService
if (!repository.existsById(id)) {
    throw new ResourceNotFoundException(id);
}
repository.deleteById(id);
```

---

## Dependências Removidas

Não reintroduzir estas dependências — foram substituídas ou eliminadas:

- `hibernate-entitymanager` — incorporado no `hibernate-core`
- `springfox-swagger2` / `springfox-swagger-ui` — substituídos por `springdoc-openapi-starter-webmvc-ui:3.0.3`
- Propriedades `security.oauth2.client.*` — namespace não existe no Spring Boot 4

---

## Status

Migração concluída em 2026-05-09. `mvnw.cmd verify` → `BUILD SUCCESS`, cobertura JaCoCo ≥ 70%, 80 testes passando.
