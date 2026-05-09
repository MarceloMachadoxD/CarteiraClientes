# Plano de Implementação: spring-boot-4-java-upgrade

## Visão Geral

Migração incremental do projeto CarteiraClientes de Java 11 + Spring Boot 2.4.5/3.0.0 para Java 25 + Spring Boot 4.0.0. Cada etapa é verificável de forma independente antes de avançar para a próxima, minimizando o risco de regressão.

---

## Tarefas

- [x] 1. Atualizar `pom.xml` para Java 25 e Spring Boot 4.0.0
  - [x] 1.1 Atualizar parent BOM, `java.version` e remover versões explícitas dos starters
    - Alterar `<version>` do `spring-boot-starter-parent` de `2.4.5` para `4.0.0`
    - Alterar `<java.version>` de `11` para `25`
    - Remover a tag `<version>3.0.0</version>` de `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation` e `spring-boot-starter-test`
    - Remover a tag `<version>2.1.214</version>` de `com.h2database:h2` (delegando ao BOM)
    - _Requisitos: 1.1, 1.2, 1.3_

  - [x] 1.2 Remover `hibernate-entitymanager` e substituir Springfox por springdoc-openapi
    - Remover o bloco `<dependency>` de `org.hibernate:hibernate-entitymanager` (versão 5.6.14.Final)
    - Remover os blocos `<dependency>` de `io.springfox:springfox-swagger2` e `io.springfox:springfox-swagger-ui`
    - Adicionar `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3`
    - _Requisitos: 2.1, 4.1, 4.2_

  - [x] 1.3 Atualizar versões de jqwik e JaCoCo
    - Alterar versão de `net.jqwik:jqwik` de `1.7.4` para `1.9.3`
    - Alterar versão do plugin `org.jacoco:jacoco-maven-plugin` de `0.8.8` para `0.8.13`
    - Confirmar que não há nenhuma `<dependency>` com `groupId` `com.fasterxml.jackson` ou `tools.jackson` no `pom.xml`
    - _Requisitos: 5.1, 6.3, 6.4_

- [x] 2. Reescrever `SwaggerConfig` para springdoc-openapi
  - [x] 2.1 Substituir implementação baseada em Springfox pelo bean `OpenAPI` do springdoc
    - Remover todos os imports do pacote `springfox.*`
    - Remover a anotação `@EnableSwagger2` e o método `api()` que retorna `Docket`
    - Adicionar imports de `io.swagger.v3.oas.models.OpenAPI` e `io.swagger.v3.oas.models.info.Info`
    - Criar método `carteiraClientesOpenAPI()` anotado com `@Bean` que retorna um `OpenAPI` com título `"CarteiraClientes API"`, descrição e versão `"1.0.0"` (strings não-vazias)
    - _Requisitos: 4.3_

- [x] 3. Checkpoint — verificar compilação após mudanças no `pom.xml` e `SwaggerConfig`
  - Executar `mvnw.cmd clean package -DskipTests` e confirmar que a última linha contém `BUILD SUCCESS`
  - _Requisitos: 1.4, 4.6_

- [x] 4. Migrar imports `javax.*` para `jakarta.*`
  - [x] 4.1 Substituir `javax.persistence.*` por `jakarta.persistence.*` nas entidades JPA
    - Em `entities/Cliente.java`: substituir `import javax.persistence.*;` por `import jakarta.persistence.*;`
    - Em `entities/Visita.java`: substituir `import javax.persistence.*;` por `import jakarta.persistence.*;`
    - Em `entities/User.java`: substituir `import javax.persistence.*;` por `import jakarta.persistence.*;`
    - Em `entities/Role.java`: substituir `import javax.persistence.*;` por `import jakarta.persistence.*;`
    - _Requisitos: 3.1_

  - [x] 4.2 Substituir `javax.validation.*` por `jakarta.validation.*` nos DTOs
    - Em `dto/ClienteDTO.java`: substituir imports `javax.validation.constraints.*` por `jakarta.validation.constraints.*`
    - Em `dto/UserDTO.java`: substituir imports `javax.validation.constraints.*` por `jakarta.validation.constraints.*`
    - Em `dto/UserInsertDTO.java`: substituir imports `javax.validation.constraints.*` por `jakarta.validation.constraints.*`
    - Em `dto/VisitaDTO.java`: substituir imports `javax.validation.constraints.*` por `jakarta.validation.constraints.*`
    - _Requisitos: 3.2_

  - [x] 4.3 Substituir `javax.servlet.*` por `jakarta.servlet.*` no handler de exceções
    - Em `resources/exceptions/ResourceExceptionHandler.java`: substituir `import javax.servlet.http.HttpServletRequest;` por `import jakarta.servlet.http.HttpServletRequest;`
    - _Requisitos: 3.3_

- [x] 5. Checkpoint — verificar compilação após migração javax→jakarta
  - Executar `mvnw.cmd clean package -DskipTests` e confirmar `BUILD SUCCESS` sem nenhuma linha contendo `cannot find symbol` referenciando `javax.persistence`, `javax.validation` ou `javax.servlet`
  - _Requisitos: 3.4_

- [x] 6. Ajustar `application.properties` para Spring Boot 4
  - [x] 6.1 Remover propriedades `security.oauth2.*` incompatíveis com Spring Boot 4
    - Remover as linhas `security.oauth2.client.client-id` e `security.oauth2.client.client-secret` do arquivo `src/main/resources/application.properties`
    - Manter as propriedades `jwt.secret` e `jwt.duration` (são custom e não causam warnings)
    - Manter `spring.profiles.active` e `spring.jpa.open-in-view=false` sem alteração
    - _Requisitos: 7.1, 7.3_

- [x] 7. Corrigir testes quebrados por mudanças de API do Spring Boot 4
  - [x] 7.1 Substituir `@SpyBean` por `@MockitoSpyBean` em `ClienteResourceTest`
    - Em `ClienteResourceTest.java`: remover o import `org.springframework.boot.test.mock.mockito.SpyBean`
    - Adicionar o import `org.springframework.test.context.bean.override.mockito.MockitoSpyBean`
    - Substituir a anotação `@SpyBean` por `@MockitoSpyBean` no campo `clienteService`
    - _Requisitos: 6.5_

  - [x] 7.2 Migrar `ClienteRepositoryPropertyTest` do padrão `@BeforeProperty` para `ApplicationContextAware`
    - Verificar se a classe usa `@BeforeProperty` + `TestContextManager` para inicializar o contexto Spring
    - Se sim: migrar para o padrão `@ExtendWith(SpringExtension.class)` + `@DataJpaTest` + `ApplicationContextAware`, com acesso ao repositório sempre via método `getRepository()`
    - Preservar todas as propriedades (`@Property`) e geradores (`@ForAll`) existentes sem alteração de lógica
    - _Requisitos: 6.6_

  - [x] 7.3 Verificar compatibilidade dos testes `@DataJpaTest` com jqwik 1.9.3
    - Executar `mvnw.cmd test -Dtest=ClienteRepositoryTest` e `mvnw.cmd test -Dtest=ClienteRepositoryPropertyTest`
    - Se houver falhas de `ClassNotFoundException` ou incompatibilidade de bytecode, ajustar conforme o padrão `ApplicationContextAware` descrito no design
    - _Requisitos: 6.3_

  - [x] 7.4 Verificar compatibilidade dos testes `@SpringBootTest` com MockMvc e Jackson 3
    - Executar `mvnw.cmd test -Dtest=ClienteResourceTest`
    - Se houver `InvalidDefinitionException` na serialização, verificar que todos os DTOs possuem construtor padrão `public XxxDTO() {}`; adicionar se ausente
    - Se houver outros erros de API do Spring Boot 4, corrigir preservando as asserções originais (status HTTP, campos do DTO, valores esperados)
    - _Requisitos: 5.4, 6.5_

- [x] 8. Checkpoint — todos os testes existentes devem passar
  - Executar `mvnw.cmd test` e confirmar `BUILD SUCCESS` com zero testes `FAILED` ou `ERROR`
  - Confirmar que o campo `Tests run:` do sumário do Maven Surefire é igual ou maior ao valor registrado antes da migração
  - _Requisitos: 2.3, 2.4, 6.1_

- [x] 9. Implementar property-based tests para as propriedades de corretude
  - [x] 9.1 Criar classe `ClienteResourcePropertyTest` com configuração base
    - Criar arquivo `src/test/java/com/github/marcelomachadoxd/carteiraclientes/resources/ClienteResourcePropertyTest.java`
    - Anotar com `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@Transactional`
    - Injetar `MockMvc` via `@Autowired` e `ClienteRepository` via `@Autowired` para setup de dados
    - Adicionar método auxiliar `buildValidClienteDTO(String nome, String email, int qtdQuartos, int qtdBanheiros, int qtdVagas, int metragem, int valorMaximo)` que retorna um `ClienteDTO` preenchido
    - _Requisitos: 9.2, 9.4_

  - [x] 9.2 Implementar Propriedade 1: busca por ID existente retorna DTO completo
    - **Propriedade 1: Busca por ID existente retorna DTO completo**
    - **Valida: Requisito 9.2**
    - Anotar com `@Property(tries = 100)` e comentário `// Feature: spring-boot-4-java-upgrade, Property 1: Busca por ID existente retorna DTO completo`
    - Usar `@ForAll @AlphaChars @StringLength(min = 1, max = 20)` para `nome`, `@ForAll @IntRange(min = 0, max = 10)` para campos numéricos
    - Salvar um `Cliente` via `ClienteRepository` com email único (`System.nanoTime() + "@pbt.test"`), executar `GET /clientes/id/{id}` e verificar HTTP 200 + presença dos campos `id`, `nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`, `obs` no JSON
    - _Requisitos: 9.2_

  - [x] 9.3 Implementar Propriedade 2: busca por ID inexistente retorna 404
    - **Propriedade 2: Busca por ID inexistente retorna 404**
    - **Valida: Requisitos 9.3, 9.10**
    - Anotar com `@Property(tries = 100)` e comentário `// Feature: spring-boot-4-java-upgrade, Property 2: Busca por ID inexistente retorna 404`
    - Usar `@ForAll @LongRange(min = 900000L, max = 999999L)` para gerar IDs que não existem no seed data
    - Executar `GET /clientes/id/{id}` e verificar HTTP 404 + presença do campo `error` no corpo JSON
    - _Requisitos: 9.3, 9.10_

  - [x] 9.4 Implementar Propriedade 3: criação com payload válido retorna 201 com Location
    - **Propriedade 3: Criação com payload válido retorna 201 com Location**
    - **Valida: Requisito 9.4**
    - Anotar com `@Property(tries = 100)` e comentário `// Feature: spring-boot-4-java-upgrade, Property 3: Criação com payload válido retorna 201 com Location`
    - Usar `@ForAll @AlphaChars @StringLength(min = 1, max = 20)` para `nome`, `@ForAll @IntRange(min = 0, max = 10)` para campos numéricos; construir email único com `System.nanoTime()`
    - Executar `POST /clientes` com o DTO serializado e verificar HTTP 201 + header `Location` presente
    - _Requisitos: 9.4_

  - [x] 9.5 Implementar Propriedade 4: criação com email inválido retorna 422 com erros de validação
    - **Propriedade 4: Criação com email inválido retorna 422 com erros de validação**
    - **Valida: Requisito 9.5**
    - Anotar com `@Property(tries = 100)` e comentário `// Feature: spring-boot-4-java-upgrade, Property 4: Criação com email inválido retorna 422 com erros de validação`
    - Usar `@ForAll @AlphaChars @StringLength(min = 1, max = 20)` para gerar strings sem `@` (emails inválidos)
    - Executar `POST /clientes` com o email inválido e verificar HTTP 422 + `errors[0].fieldName == "email"` no corpo JSON
    - _Requisitos: 9.5_

  - [x] 9.6 Implementar Propriedade 5: atualização com payload válido retorna 200
    - **Propriedade 5: Atualização com payload válido retorna 200**
    - **Valida: Requisito 9.6**
    - Anotar com `@Property(tries = 100)` e comentário `// Feature: spring-boot-4-java-upgrade, Property 5: Atualização com payload válido retorna 200`
    - Salvar um `Cliente` via `ClienteRepository`, executar `PUT /clientes/id/{id}` com payload válido e verificar HTTP 200 sem corpo
    - _Requisitos: 9.6_

- [x] 10. Checkpoint — todos os testes (incluindo property tests) devem passar
  - Executar `mvnw.cmd test` e confirmar `BUILD SUCCESS` com zero falhas
  - _Requisitos: 6.1_

- [x] 11. Verificar cobertura JaCoCo
  - [x] 11.1 Executar `mvnw.cmd verify` e confirmar cobertura LINE ≥ 70%
    - Executar `mvnw.cmd verify` e verificar que o relatório `target/site/jacoco/index.html` reporta cobertura de linhas ≥ 70%
    - As classes `CarteiraClientesApplication` e `config/SwaggerConfig` já estão excluídas na configuração do plugin
    - Se a cobertura estiver abaixo de 70%, identificar as classes descobertas e adicionar testes unitários nas classes de serviço ou resource correspondentes
    - _Requisitos: 6.2_

- [x] 12. Atualizar steering files
  - [x] 12.1 Atualizar `.kiro/steering/tech.md` com a nova stack
    - Substituir Java `11` por `25`
    - Substituir Spring Boot parent BOM `2.4.5` por `4.0.0`
    - Remover linha de `hibernate-entitymanager`
    - Substituir `springfox-swagger2 + swagger-ui 2.9.2` por `springdoc-openapi-starter-webmvc-ui 3.0.3`
    - Atualizar jqwik de `1.7.4` para `1.9.3`
    - Atualizar H2 de `2.1.214 (explícito)` para `gerenciado pelo BOM`
    - Atualizar JaCoCo de `0.8.8` para `0.8.13`
    - Atualizar path do Swagger UI de `/swagger-ui.html` para `/swagger-ui/index.html`
    - Remover a nota de inconsistência entre parent BOM e starters
    - _Requisitos: 8.1, 8.2, 8.3_

  - [x] 12.2 Criar `.kiro/steering/migration-spring-boot-4.md` com registro da migração
    - Criar o arquivo com as seções obrigatórias (cada uma com ao menos uma entrada): **Substituições Realizadas**, **Imports Migrados**, **Problemas Encontrados e Soluções**, **Propriedades Alteradas** e **Status da Migração**
    - Preencher **Substituições Realizadas** com a tabela de dependências removidas e adicionadas (com versões)
    - Preencher **Imports Migrados** com a lista de pacotes `javax.*` substituídos por `jakarta.*` e os arquivos afetados (`Cliente.java`, `Visita.java`, `User.java`, `Role.java`, `ClienteDTO.java`, `UserDTO.java`, `UserInsertDTO.java`, `VisitaDTO.java`, `ResourceExceptionHandler.java`)
    - Preencher **Propriedades Alteradas** com a remoção das chaves `security.oauth2.client.client-id` e `security.oauth2.client.client-secret`
    - Preencher **Status da Migração** com `Migração concluída com sucesso` e a data de conclusão no formato `YYYY-MM-DD` (após `mvnw.cmd verify` retornar `BUILD SUCCESS` com cobertura ≥ 70%)
    - _Requisitos: 8.4, 8.5_

---

## Notas

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- A ordem das tarefas segue a estratégia incremental do design: cada etapa é verificável antes de avançar
- A migração javax→jakarta (tarefas 4.1–4.3) deve ser feita **antes** dos checkpoints de testes, pois sem ela o build falha com `cannot find symbol`
- Os property tests (9.2–9.6) usam o padrão `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`, diferente dos testes de repositório que usam `@DataJpaTest` + `ApplicationContextAware`
- Emails gerados nos property tests devem usar `System.nanoTime()` como sufixo para evitar violações de constraint de unicidade entre as 100 iterações
- O `application-test.properties` não requer nenhuma alteração (todas as propriedades são válidas no Spring Boot 4)
- `@SpyBean` foi removido no Spring Boot 4; usar `@MockitoSpyBean` do pacote `org.springframework.test.context.bean.override.mockito`

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["4.1", "4.2", "4.3"] },
    { "id": 3, "tasks": ["6.1"] },
    { "id": 4, "tasks": ["7.1", "7.2", "7.3", "7.4"] },
    { "id": 5, "tasks": ["9.1"] },
    { "id": 6, "tasks": ["9.2", "9.3", "9.4", "9.5", "9.6"] },
    { "id": 7, "tasks": ["11.1"] },
    { "id": 8, "tasks": ["12.1", "12.2"] }
  ]
}
```
