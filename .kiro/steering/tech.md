---
inclusion: always
---

# Tech Stack

## Core Dependencies

| Biblioteca | Versão | Escopo |
|---|---|---|
| Java | 11 | runtime |
| Spring Boot (parent BOM) | 2.4.5 | build |
| spring-boot-starter-web | 3.0.0 | compile |
| spring-boot-starter-data-jpa | 3.0.0 | compile |
| spring-boot-starter-validation | 3.0.0 | compile |
| hibernate-entitymanager | 5.6.14.Final | compile |
| H2 Database | 2.1.214 | runtime |
| springfox-swagger2 + swagger-ui | 2.9.2 | compile |
| spring-boot-starter-test | 3.0.0 | test |
| jqwik | 1.7.4 | test |
| JaCoCo Maven Plugin | 0.8.8 | build |

> **Atenção:** O parent BOM é Spring Boot 2.4.5, mas os starters individuais estão fixados em 3.0.0. Não altere versões sem verificar compatibilidade entre essas duas linhas.

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
- `APP_PROFILE`, `CLIENT_ID`, `CLIENT_SECRET`, `JWT_SECRET`, `JWT_DURATION`

## Cobertura de Código (JaCoCo)

- Meta mínima: **70% de cobertura de linhas** (verificada em `mvnw.cmd verify`)
- Classes excluídas da cobertura: `CarteiraClientesApplication`, `config/SwaggerConfig`
- Relatório gerado em `target/site/jacoco/index.html`

## Acesso em Desenvolvimento

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
- Coleções Postman: `src/main/resources/postman-requests/`
