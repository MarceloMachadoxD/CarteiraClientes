---
inclusion: always
---

# Tech Stack

## Core Dependencies

| Biblioteca | Versão | Escopo |
|---|---|---|
| Java | 25 | runtime |
| Spring Boot (parent BOM) | 4.0.0 | build |
| spring-boot-starter-web | gerenciado pelo BOM | compile |
| spring-boot-starter-data-jpa | gerenciado pelo BOM | compile |
| spring-boot-starter-validation | gerenciado pelo BOM | compile |
| H2 Database | gerenciado pelo BOM | runtime |
| springdoc-openapi-starter-webmvc-ui | 3.0.3 | compile |
| spring-boot-starter-test | gerenciado pelo BOM | test |
| jqwik | 1.9.3 | test |
| JaCoCo Maven Plugin | 0.8.13 | build |

## Build System

- **Maven Wrapper** — usar sempre `mvnw.cmd` no Windows (nunca `mvn` diretamente)
- Group ID: `com.github.marcelomachadoxd` | Artifact ID: `CarteiraClientes`

### Comandos Windows

```cmd
# Rodar a aplicação (perfil test ativo por padrão)
mvnw.cmd spring-boot:run

# Build sem testes
mvnw.cmd clean package -DskipTests

# Rodar todos os testes
mvnw.cmd test

# Rodar classe específica
mvnw.cmd test -Dtest=ClienteRepositoryTest

# Rodar método específico
mvnw.cmd test -Dtest=ClienteRepositoryTest#nomeDoMetodo

# Verificar cobertura JaCoCo (mínimo 70% de linhas)
mvnw.cmd verify
```

## Perfis de Configuração

### `test` (padrão)
- H2 in-memory (`jdbc:h2:mem:testdb`, user: `sa`, sem senha)
- H2 Console em `/h2-console`
- SQL logging habilitado
- Seed data carregado de `src/main/resources/data.sql`

### Produção
Requer variáveis de ambiente externas:
- `APP_PROFILE`, `JWT_SECRET`, `JWT_DURATION`

## Cobertura de Código (JaCoCo)

- Meta mínima: **70% de cobertura de linhas** (verificada em `mvnw.cmd verify`)
- Classes excluídas da cobertura: `CarteiraClientesApplication`, `config/SwaggerConfig`
- Relatório gerado em `target/site/jacoco/index.html`

## Acesso em Desenvolvimento

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console`
- Coleções Postman: `src/main/resources/postman-requests/`
