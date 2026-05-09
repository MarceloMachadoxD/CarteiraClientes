# Documento de Requisitos

## Introduction

Atualização da stack do projeto CarteiraClientes de uma configuração desatualizada e inconsistente (Spring Boot 2.4.5 como parent BOM com starters fixados em 3.0.0, Java 11, Springfox 2.9.2, Hibernate EntityManager 5.6.14, imports `javax.*`) para uma stack moderna e coesa: Java 25 + Spring Boot 4.0.0 (Spring Framework 7).

O objetivo é modernizar o projeto com o mínimo de impacto possível na lógica de negócio existente, substituindo apenas o que é incompatível ou obsoleto. Isso inclui: atualizar o `pom.xml`, migrar todos os imports `javax.*` para `jakarta.*` (Jakarta EE 11), substituir o Springfox pelo springdoc-openapi, adaptar o código ao Jackson 3 (padrão no Spring Boot 4), e atualizar a documentação interna (steering files) para refletir a nova stack.

---

## Glossary

- **Build_System**: O Maven Wrapper (`mvnw.cmd`) e o `pom.xml` que definem dependências, plugins e ciclo de build do projeto.
- **Application**: A aplicação Spring Boot em execução, iniciada via `mvnw.cmd spring-boot:run`.
- **SwaggerConfig**: Classe `config/SwaggerConfig.java` responsável pela configuração da documentação OpenAPI/Swagger da API.
- **Springfox**: Biblioteca `springfox-swagger2` + `springfox-swagger-ui` v2.9.2, incompatível com Spring Boot 3+, que deve ser substituída.
- **springdoc-openapi**: Biblioteca substituta do Springfox, compatível com Spring Boot 4, que gera documentação OpenAPI 3 automaticamente. Artefato: `org.springdoc:springdoc-openapi-starter-webmvc-ui`, versão mínima `3.0.0`.
- **hibernate-entitymanager**: Artefato legado do Hibernate 5 removido no Hibernate 6+; sua funcionalidade é incorporada diretamente no `hibernate-core` gerenciado pelo Spring Boot 4.
- **JaCoCo**: Plugin Maven de cobertura de código; a meta mínima de 70% de cobertura de linhas deve ser mantida após a migração. Versão mínima necessária para suporte ao Java 25: `0.8.13`.
- **jqwik**: Biblioteca de property-based testing usada nos testes do projeto. Versão compatível com Java 25 e JUnit Platform 1.12: `1.9.3`.
- **Jakarta_EE**: Especificação que substituiu o Java EE. A partir do Spring Boot 3, todos os pacotes `javax.*` (persistence, validation, servlet) foram renomeados para `jakarta.*`. O projeto ainda usa `javax.*` e precisa migrar.
- **Jackson_3**: Versão principal do Jackson adotada como padrão no Spring Boot 4 / Spring Framework 7. O Spring Boot 4 gerencia a versão via BOM; nenhuma dependência Jackson explícita deve ser declarada no `pom.xml`.
- **Steering_Files**: Arquivos Markdown em `.kiro/steering/` que documentam a stack, estrutura e convenções do projeto para uso pelo assistente de IA.
- **Migration_Steering**: Steering file em `.kiro/steering/migration-spring-boot-4.md` que registra decisões, problemas encontrados e soluções aplicadas durante a migração.
- **Perfil_Test**: Perfil Spring `test` que usa H2 in-memory com seed data carregado de `data.sql`.

---

## Requirements

### Requirement 1: Atualização do Build System para Java 25 e Spring Boot 4.0.0

**User Story:** Como desenvolvedor, quero que o `pom.xml` use Java 25 e Spring Boot 4.0.0 como única fonte de verdade para versões de dependências, para que a stack seja coesa e sem inconsistências.

#### Acceptance Criteria

1. THE **Build_System** SHALL declarar `<java.version>25</java.version>` nas `<properties>` do `pom.xml`, substituindo o valor anterior `11`.
2. THE **Build_System** SHALL declarar o `spring-boot-starter-parent` na versão `4.0.0` como único parent BOM do projeto, sem nenhum BOM adicional importado via `<dependencyManagement>`.
3. THE **Build_System** SHALL remover as tags `<version>` explícitas dos starters `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation` e `spring-boot-starter-test`, delegando o gerenciamento de versões ao parent BOM. As dependências não gerenciadas pelo BOM — `jqwik`, `springdoc-openapi-starter-webmvc-ui` e `h2` — devem manter suas versões explícitas; as dependências `springfox-swagger2`, `springfox-swagger-ui` e `hibernate-entitymanager` devem ser removidas integralmente.
4. WHEN o comando `mvnw.cmd clean package -DskipTests` for executado, THE **Build_System** SHALL completar o build e a última linha de saída do Maven SHALL conter `BUILD SUCCESS`.
5. IF o `pom.xml` contiver uma tag `<version>` explícita com valor diferente do gerenciado pelo BOM 4.0.0 para qualquer starter Spring Boot, THEN THE **Build_System** SHALL falhar com `BUILD FAILURE` e a saída SHALL conter uma mensagem indicando conflito de versão.

---

### Requirement 2: Remoção do hibernate-entitymanager

**User Story:** Como desenvolvedor, quero remover a dependência `hibernate-entitymanager` obsoleta, para que o projeto use apenas as dependências JPA gerenciadas pelo Spring Boot 4.

#### Acceptance Criteria

1. THE **Build_System** SHALL remover a entrada `<dependency>` com `groupId` `org.hibernate` e `artifactId` `hibernate-entitymanager` do `pom.xml`, sem adicionar nenhuma outra `<dependency>` com `groupId` `org.hibernate` ou `org.hibernate.orm` manualmente em substituição.
2. WHEN o projeto for compilado após a remoção com `mvnw.cmd clean package -DskipTests`, THE **Build_System** SHALL produzir saída com código de saída `0` e a última linha SHALL conter `BUILD SUCCESS`, sem nenhuma linha contendo `ClassNotFoundException` ou `NoClassDefFoundError`.
3. WHEN os testes forem executados com `mvnw.cmd test`, THE **Build_System** SHALL executar todos os testes do projeto com resultado `BUILD SUCCESS`, sem nenhuma linha de saída contendo os termos `Hibernate`, `JPA`, `jakarta.persistence` ou `org.hibernate` em mensagens de falha ou erro.
4. WHEN `mvnw.cmd test` for executado antes e depois da remoção do `hibernate-entitymanager`, THE **Build_System** SHALL reportar o mesmo valor no campo `Tests run:` do sumário do Maven Surefire em ambas as execuções, garantindo que nenhum teste foi silenciosamente removido.

---

### Requirement 3: Migração de javax.* para jakarta.*

**User Story:** Como desenvolvedor, quero substituir todos os imports `javax.*` por `jakarta.*` no código-fonte, para que o projeto seja compatível com Jakarta EE 11 exigido pelo Spring Boot 4.

#### Acceptance Criteria

1. WHEN a migração for concluída, THE **Build_System** SHALL não conter nenhum import do pacote `javax.persistence.*` em nenhum arquivo `.java` do projeto. WHEN o build for executado com `mvnw.cmd clean package -DskipTests`, todos os imports de anotações JPA (`@Entity`, `@Table`, `@Id`, `@Column`, `@OneToMany`, `@ManyToMany`, `@JoinTable`, `@JoinColumn`, `@GeneratedValue`, `@ManyToOne`) SHALL usar o pacote `jakarta.persistence.*`.
2. WHEN a migração for concluída, THE **Build_System** SHALL não conter nenhum import do pacote `javax.validation.*` em nenhum arquivo `.java` do projeto. WHEN o build for executado, todos os imports de anotações Bean Validation (`@Email`, `@NotBlank`, `@DecimalMin`, `@Valid`) SHALL usar o pacote `jakarta.validation.*`.
3. WHEN a migração for concluída, THE **Build_System** SHALL não conter nenhum import do pacote `javax.servlet.*` em nenhum arquivo `.java` do projeto. WHEN o build for executado, o import `javax.servlet.http.HttpServletRequest` em `ResourceExceptionHandler` SHALL estar substituído por `jakarta.servlet.http.HttpServletRequest`.
4. WHEN o comando `mvnw.cmd clean package -DskipTests` for executado após a substituição dos imports, THE **Build_System** SHALL produzir `BUILD SUCCESS` sem nenhuma linha de saída contendo `cannot find symbol` referenciando classes dos pacotes `javax.persistence`, `javax.validation` ou `javax.servlet`.

---

### Requirement 4: Substituição do Springfox pelo springdoc-openapi

**User Story:** Como desenvolvedor, quero substituir o Springfox (incompatível com Spring Boot 3+) pelo springdoc-openapi, para que a documentação da API continue disponível e funcional na nova stack.

#### Acceptance Criteria

1. THE **Build_System** SHALL remover as entradas `<dependency>` com `groupId` `io.springfox` e `artifactId` `springfox-swagger2` e `springfox-swagger-ui` do `pom.xml`.
2. THE **Build_System** SHALL adicionar a dependência `org.springdoc:springdoc-openapi-starter-webmvc-ui` com versão mínima `3.0.0` ao `pom.xml`, com a versão declarada explicitamente pois não é gerenciada pelo BOM do Spring Boot 4.
3. THE **SwaggerConfig** SHALL ser reescrita de forma que: (a) não contenha nenhum import do pacote `springfox.*`; (b) não use a anotação `@EnableSwagger2` nem a classe `Docket`; (c) use a API do springdoc-openapi (bean `OpenAPI` ou `GroupedOpenApi`) para configurar título, descrição e versão da documentação com strings não-vazias.
4. WHEN a **Application** for iniciada com o perfil `test`, THE **SwaggerConfig** SHALL disponibilizar a documentação OpenAPI no path `/swagger-ui/index.html`, retornando HTTP 200 para uma requisição GET nesse endpoint.
5. WHEN a **Application** for iniciada com o perfil `test`, THE **SwaggerConfig** SHALL disponibilizar a especificação OpenAPI em formato JSON no path `/v3/api-docs`, retornando HTTP 200 para uma requisição GET nesse endpoint.
6. IF as dependências Springfox forem removidas do `pom.xml` e não houver nenhuma referência a classes ou imports do pacote `springfox.*` no código-fonte, THEN THE **Build_System** SHALL completar a compilação com `BUILD SUCCESS`.

---

### Requirement 5: Compatibilidade com Jackson 3

**User Story:** Como desenvolvedor, quero que o projeto seja compatível com o Jackson 3 adotado pelo Spring Boot 4, para que a serialização e desserialização JSON continuem funcionando corretamente.

#### Acceptance Criteria

1. THE **Build_System** SHALL não conter nenhuma `<dependency>` com `groupId` `com.fasterxml.jackson` ou `tools.jackson` no `pom.xml`; o gerenciamento de versão do Jackson SHALL ser delegado integralmente ao BOM do Spring Boot 4.
2. WHEN uma requisição `GET /clientes/id/{id}` for feita com um id existente após a migração, THE **Application** SHALL retornar HTTP 200 com um corpo JSON válido contendo os campos `id`, `nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo` e `obs`, sem erros de serialização.
3. WHEN uma requisição `POST /clientes` for feita com payload JSON contendo os campos `nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem` e `valorMaximo` com valores válidos após a migração, THE **Application** SHALL desserializar o corpo da requisição corretamente e retornar HTTP 201, sem erros de desserialização como `InvalidDefinitionException` ou `MismatchedInputException`.
4. IF o código-fonte em `src/main` ou `src/test` contiver imports diretos de classes do pacote `com.fasterxml.jackson.*` que foram renomeadas ou movidas no Jackson 3, THEN THE **Build_System** SHALL ter esses imports atualizados para os pacotes equivalentes do Jackson 3, mantendo o comportamento de serialização original.

---

### Requirement 6: Compatibilidade dos Testes com a Nova Stack

**User Story:** Como desenvolvedor, quero que todos os testes existentes continuem passando após a migração, para que a lógica de negócio seja preservada e a cobertura mínima seja mantida.

#### Acceptance Criteria

1. WHEN o comando `mvnw.cmd test` for executado após a migração, THE **Build_System** SHALL produzir `BUILD SUCCESS` com zero testes com status `FAILED` ou `ERROR`, e o valor do campo `Tests run:` no sumário do Maven Surefire SHALL ser igual ou maior ao valor registrado antes da migração.
2. WHEN o comando `mvnw.cmd verify` for executado, THE **JaCoCo** SHALL reportar cobertura de linhas (`LINE`) igual ou superior a 70% no relatório `target/site/jacoco/index.html`, excluindo as classes `CarteiraClientesApplication` e `SwaggerConfig` da contagem.
3. WHEN o build for executado com `mvnw.cmd test`, THE **Build_System** SHALL declarar a dependência `net.jqwik:jqwik` com versão `1.9.3` explícita no `pom.xml`, de forma que os testes anotados com `@Property` e `@ForAll` compilem e executem sem `ClassNotFoundException` ou erros de incompatibilidade de bytecode com Java 25.
4. WHEN o build for executado com `mvnw.cmd verify`, THE **Build_System** SHALL declarar o plugin `jacoco-maven-plugin` com versão `0.8.13` no `pom.xml`, substituindo a versão anterior `0.8.8`, pois a versão `0.8.13` é a mínima com suporte ao bytecode do Java 25 (classfile version 69).
5. IF algum teste falhar após a migração por uso de `@SpyBean` (removido no Spring Boot 4 em favor de `@MockitoSpyBean`), ou por mudança de API em `MockMvc`, `@SpringBootTest` ou `@DataJpaTest`, THEN THE **Build_System** SHALL ter o teste corrigido para usar a API equivalente da nova versão, preservando as asserções originais sem alteração.
6. IF a classe `ClienteRepositoryPropertyTest` usar `@BeforeProperty` com `TestContextManager` para inicializar o contexto Spring, THEN THE **Build_System** SHALL migrar essa classe para o padrão `@ExtendWith(SpringExtension.class)` + `@DataJpaTest`, preservando todas as propriedades (`@Property`) e geradores (`@ForAll`) existentes sem alteração.

---

### Requirement 7: Compatibilidade das Propriedades de Configuração

**User Story:** Como desenvolvedor, quero que os arquivos `application.properties` e `application-test.properties` sejam compatíveis com Spring Boot 4, para que a aplicação inicialize corretamente nos perfis `test` e produção.

#### Acceptance Criteria

1. WHEN a **Application** for iniciada com o perfil `test` via `mvnw.cmd spring-boot:run`, THE **Perfil_Test** SHALL inicializar com código de saída `0` e o banco H2 in-memory SHALL estar acessível na URL `jdbc:h2:mem:testdb` com usuário `sa` e senha vazia.
2. WHEN a **Application** inicializar com o perfil `test`, THE **Perfil_Test** SHALL carregar o seed data de `src/main/resources/data.sql` sem erros, de forma que uma requisição `GET /clientes` retorne ao menos um registro com HTTP 200.
3. IF alguma chave de propriedade em `application.properties` ou `application-test.properties` for removida ou renomeada no Spring Boot 4 — incluindo chaves dos grupos `spring.datasource.*`, `spring.jpa.*`, `spring.h2.console.*` e `security.oauth2.*` — THEN THE **Build_System** SHALL substituir a chave pelo nome equivalente da nova versão, mantendo o mesmo valor configurado anteriormente.
4. WHEN a **Application** estiver rodando com o perfil `test`, THE **Perfil_Test** SHALL disponibilizar o H2 Console em `/h2-console`, retornando HTTP 200 para uma requisição GET nesse path.

---

### Requirement 8: Atualização dos Steering Files

**User Story:** Como desenvolvedor, quero que os steering files reflitam a nova stack após a migração, para que o assistente de IA tenha informações corretas sobre o projeto.

#### Acceptance Criteria

1. WHEN a migração for concluída, THE **Steering_Files** SHALL ter o arquivo `.kiro/steering/tech.md` atualizado com as seguintes versões: Java 25, Spring Boot 4.0.0, `springdoc-openapi-starter-webmvc-ui` versão mínima `3.0.0`, jqwik `1.9.3`, H2 Database (versão gerenciada pelo BOM do Spring Boot 4) e JaCoCo `0.8.13`.
2. WHEN a migração for concluída, THE **Steering_Files** SHALL remover do `tech.md` todas as ocorrências de: `hibernate-entitymanager`, `springfox-swagger2`, `springfox-swagger-ui`, versão `2.4.5` do parent BOM, versão `3.0.0` dos starters, e a nota de inconsistência entre parent BOM e starters.
3. WHEN a migração for concluída, THE **Steering_Files** SHALL atualizar no `tech.md` o path do Swagger UI de `/swagger-ui.html` para `/swagger-ui/index.html`, refletindo o path padrão do springdoc-openapi.
4. WHEN a migração for concluída, THE **Migration_Steering** SHALL ser criado em `.kiro/steering/migration-spring-boot-4.md` contendo obrigatoriamente as seguintes seções, cada uma com ao menos uma entrada: (a) **Substituições Realizadas** — lista de dependências removidas e adicionadas com versões; (b) **Imports Migrados** — lista de pacotes `javax.*` substituídos por `jakarta.*` com os arquivos afetados; (c) **Problemas Encontrados e Soluções** — cada problema com descrição, causa raiz e solução aplicada; (d) **Propriedades Alteradas** — chaves renomeadas ou removidas com os valores antigos e novos.
5. WHEN o comando `mvnw.cmd verify` retornar `BUILD SUCCESS` e o relatório JaCoCo reportar cobertura de linhas igual ou superior a 70%, THE **Migration_Steering** SHALL conter uma seção **Status da Migração** com o texto `Migração concluída com sucesso` e a data de conclusão no formato `YYYY-MM-DD`.

---

### Requirement 9: Preservação da Lógica de Negócio

**User Story:** Como desenvolvedor, quero que toda a lógica de negócio, estrutura de pacotes e convenções do projeto sejam preservadas durante a migração, para que o comportamento da API não seja alterado.

#### Acceptance Criteria

1. THE **Build_System** SHALL não modificar nenhum arquivo nas camadas `entities`, `dto`, `repositories`, `services` ou `resources`, exceto quando a modificação for necessária para substituir uma API removida ou renomeada no Java 25, Spring Boot 4 ou Jakarta EE 11 — nesse caso, apenas imports e anotações de API SHALL ser alterados, sem modificar lógica de negócio, asserções de teste ou valores esperados.
2. WHEN uma requisição `GET /clientes/id/{id}` for feita com um id existente após a migração, THE **Application** SHALL retornar HTTP 200 com um corpo JSON contendo os campos `id`, `nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo` e `obs`.
3. WHEN uma requisição `GET /clientes/id/{id}` for feita com id inexistente após a migração, THE **Application** SHALL retornar HTTP 404 com corpo `StandardError`.
4. WHEN uma requisição `POST /clientes` for feita com payload válido após a migração, THE **Application** SHALL retornar HTTP 201 com header `Location` apontando para o recurso criado.
5. WHEN uma requisição `POST /clientes` for feita com payload inválido (campos obrigatórios ausentes ou com formato incorreto) após a migração, THE **Application** SHALL retornar HTTP 422 com corpo `ValidationError` contendo lista de `FieldMessage`.
6. WHEN uma requisição `PUT /clientes/id/{id}` for feita com id existente e payload válido após a migração, THE **Application** SHALL retornar HTTP 200 sem corpo.
7. WHEN uma requisição `DELETE /clientes/id/{id}` for feita com id existente após a migração, THE **Application** SHALL retornar HTTP 204 sem corpo.
8. WHEN uma requisição `DELETE /clientes/id/{id}` for feita com id inexistente após a migração, THE **Application** SHALL retornar HTTP 404 com corpo `StandardError`.
9. THE **Build_System** SHALL preservar a estrutura de pacotes `com.github.marcelomachadoxd.carteiraclientes` e todos os nomes de classes, métodos e campos existentes nas camadas de negócio, sem renomeações além das exigidas por incompatibilidade de API.
10. IF uma exceção `ResourceNotFoundException` for lançada por um service após a migração, THEN THE **ResourceExceptionHandler** SHALL mapeá-la para HTTP 404. IF uma exceção `DatabaseException` for lançada, THEN THE **ResourceExceptionHandler** SHALL mapeá-la para HTTP 400. IF uma exceção `MethodArgumentNotValidException` for lançada, THEN THE **ResourceExceptionHandler** SHALL mapeá-la para HTTP 422 com corpo `ValidationError` — preservando o comportamento do `@ControllerAdvice` existente.
