# Documento de Design Técnico

## Feature: spring-boot-4-java-upgrade

---

## Visão Geral

Este documento descreve a estratégia técnica para migrar o projeto CarteiraClientes de uma stack inconsistente (Spring Boot 2.4.5 como parent BOM com starters fixados em 3.0.0, Java 11, Springfox 2.9.2, `hibernate-entitymanager` 5.6.14) para uma stack moderna e coesa: **Java 25 + Spring Boot 4.0.0** (Spring Framework 7).

A migração é incremental e orientada a risco mínimo: cada etapa é independente, verificável por `mvnw.cmd test` ou `mvnw.cmd clean package -DskipTests`, e não altera a lógica de negócio existente. O objetivo final é um projeto que compila, testa e executa corretamente na nova stack, com cobertura JaCoCo ≥ 70% e documentação OpenAPI funcional.

### Resumo das mudanças

| Componente | Estado atual | Estado alvo |
|---|---|---|
| Java | 11 | 25 |
| Spring Boot parent BOM | 2.4.5 | 4.0.0 |
| Starters (web, data-jpa, validation, test) | 3.0.0 (explícito) | gerenciado pelo BOM 4.0.0 |
| `hibernate-entitymanager` | 5.6.14.Final | removido (incorporado no `hibernate-core`) |
| Documentação OpenAPI | Springfox 2.9.2 | springdoc-openapi 3.0.3 |
| `SwaggerConfig` | `@EnableSwagger2` + `Docket` | bean `OpenAPI` do springdoc |
| jqwik | 1.7.4 | 1.9.3 |
| JaCoCo | 0.8.8 | 0.8.13 |
| `security.oauth2.*` (application.properties) | presente (legado) | removido ou comentado |
| H2 `MODE=LEGACY` (application-test.properties) | presente | mantido (compatível com H2 gerenciado pelo BOM 4) |
| imports `javax.*` | `javax.persistence.*`, `javax.validation.*`, `javax.servlet.*` | `jakarta.persistence.*`, `jakarta.validation.*`, `jakarta.servlet.*` |
| Jackson | Jackson 2 (via Spring Boot 3.0.0 starters) | Jackson 3 (gerenciado pelo BOM 4.0.0) |
| imports `javax.*` | `javax.persistence.*`, `javax.validation.*`, `javax.servlet.*` | `jakarta.persistence.*`, `jakarta.validation.*`, `jakarta.servlet.*` |
| Jackson | Jackson 2 (via Spring Boot 3.0.0 starters) | Jackson 3 (gerenciado pelo BOM 4.0.0) |

---

## Arquitetura

A migração não altera a arquitetura em camadas do projeto. O fluxo `Resource → Service → Repository` é preservado integralmente. As mudanças são restritas à camada de infraestrutura (build, configuração, documentação) e não tocam as camadas `entities`, `dto`, `repositories`, `services` ou `resources`, exceto quando uma API foi removida ou renomeada no Spring Boot 4.

### Diagrama de dependências (sem alteração)

```
ClienteResource
    └── ClienteService
            └── ClienteRepository (JpaRepository<Cliente, Long>)
```

### Estratégia de migração incremental

A ordem das etapas minimiza o risco de regressão. Cada etapa deve ser verificada antes de avançar para a próxima:

```
Etapa 1: Atualizar pom.xml (parent BOM + java.version + remoção de versões explícitas)
    ↓ verificar: mvnw.cmd clean package -DskipTests → BUILD SUCCESS
Etapa 2: Remover hibernate-entitymanager
    ↓ verificar: mvnw.cmd test → BUILD SUCCESS
Etapa 2b: Migrar imports javax.* → jakarta.*
    ↓ verificar: mvnw.cmd clean package -DskipTests → BUILD SUCCESS, sem "cannot find symbol"
Etapa 3: Substituir Springfox por springdoc-openapi + reescrever SwaggerConfig
    ↓ verificar: mvnw.cmd clean package -DskipTests → BUILD SUCCESS
Etapa 4: Ajustar application.properties (remover security.oauth2.*)
    ↓ verificar: mvnw.cmd spring-boot:run → sem WARN de propriedades desconhecidas
Etapa 5: Corrigir testes quebrados por mudanças de API do Spring Boot 4
    ↓ verificar: mvnw.cmd test → BUILD SUCCESS, zero falhas
Etapa 6: Verificar cobertura JaCoCo
    ↓ verificar: mvnw.cmd verify → cobertura LINE ≥ 70%
Etapa 7: Atualizar steering files
```

---

## Componentes e Interfaces

### 1. `pom.xml` — Alterações de build

#### 1.1 Parent BOM e versão do Java

```xml
<!-- ANTES -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.4.5</version>
</parent>
<properties>
    <java.version>11</java.version>
</properties>

<!-- DEPOIS -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version>
</parent>
<properties>
    <java.version>25</java.version>
</properties>
```

#### 1.2 Starters — remoção de versões explícitas

Os starters `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation` e `spring-boot-starter-test` devem ter suas tags `<version>` removidas. O BOM 4.0.0 gerencia as versões corretas automaticamente.

```xml
<!-- ANTES (exemplo) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.0.0</version>  <!-- REMOVER esta linha -->
</dependency>

<!-- DEPOIS -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

#### 1.3 Remoção do `hibernate-entitymanager`

O artefato `org.hibernate:hibernate-entitymanager` foi descontinuado no Hibernate 6 e não existe no Hibernate 7 (gerenciado pelo Spring Boot 4). Sua funcionalidade está incorporada diretamente no `hibernate-core`. A entrada deve ser removida sem substituto manual.

```xml
<!-- REMOVER completamente -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-entitymanager</artifactId>
    <version>5.6.14.Final</version>
</dependency>
```

#### 1.4 Substituição do Springfox pelo springdoc-openapi

```xml
<!-- REMOVER -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>

<!-- ADICIONAR -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

**Justificativa da versão:** springdoc-openapi 3.x é a linha compatível com Spring Boot 4 e Jackson 3 (que o Spring Boot 4 adota como serializador padrão). A versão 2.x é compatível apenas com Spring Boot 3.x. A versão 3.0.3 é a mais recente disponível no Maven Central no momento da escrita deste documento. ([Fonte: Maven Central](https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui))

#### 1.5 Atualização do jqwik

```xml
<!-- ANTES -->
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.7.4</version>
    <scope>test</scope>
</dependency>

<!-- DEPOIS -->
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.9.3</version>
    <scope>test</scope>
</dependency>
```

**Justificativa:** jqwik 1.9.3 é a versão mais recente disponível no Maven Central, compilada para Java 17+ e compatível com Java 25. A versão 1.7.4 foi compilada para Java 11 e pode apresentar problemas de bytecode com Java 25. ([Fonte: Maven Central](https://central.sonatype.com/artifact/net.jqwik/jqwik))

#### 1.6 Atualização do JaCoCo

```xml
<!-- ANTES -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    ...
</plugin>

<!-- DEPOIS -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.13</version>
    ...
</plugin>
```

**Justificativa:** JaCoCo 0.8.13 adicionou suporte a Java 25 (bytecode version 69). A versão 0.8.8 não reconhece o bytecode do Java 25 e falha na instrumentação. ([Fonte: eclemma.org](http://www.eclemma.org/jacoco/trunk/doc/))

#### 1.7 H2 — remoção de versão explícita

O H2 2.1.214 deve ter sua versão explícita removida, delegando ao BOM do Spring Boot 4 a versão gerenciada (H2 2.3.x ou superior).

```xml
<!-- ANTES -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.1.214</version>
    <scope>runtime</scope>
</dependency>

<!-- DEPOIS -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### 2. Migração de `javax.*` para `jakarta.*`

O projeto usa `javax.*` em todas as camadas (entidades, DTOs, handler de exceções). O Spring Boot 4 exige Jakarta EE 11, cujos pacotes foram renomeados de `javax.*` para `jakarta.*` a partir do Spring Boot 3. Esta etapa deve ser executada **antes** da substituição do Springfox (Etapa 2b na estratégia incremental), pois erros de compilação por imports ausentes bloqueariam as etapas seguintes.

#### 2.1 Arquivos afetados e mapeamento de imports

**Entidades JPA** — substituir `javax.persistence.*` por `jakarta.persistence.*`:

| Arquivo | Import atual | Import alvo |
|---|---|---|
| `entities/Cliente.java` | `import javax.persistence.*;` | `import jakarta.persistence.*;` |
| `entities/Visita.java` | `import javax.persistence.*;` | `import jakarta.persistence.*;` |
| `entities/User.java` | `import javax.persistence.*;` | `import jakarta.persistence.*;` |
| `entities/Role.java` | `import javax.persistence.*;` | `import jakarta.persistence.*;` |

Anotações afetadas: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@JoinTable`, `@JoinColumn`.

**DTOs com Bean Validation** — substituir `javax.validation.constraints.*` por `jakarta.validation.constraints.*`:

| Arquivo | Anotações afetadas |
|---|---|
| `dto/ClienteDTO.java` | `@Email`, `@DecimalMin` |
| `dto/UserDTO.java` | `@NotBlank`, `@Email` |
| `dto/UserInsertDTO.java` | `@NotBlank`, `@Email` |
| `dto/VisitaDTO.java` | `@NotNull`, `@PastOrPresent` |

**Handler de exceções** — substituir `javax.servlet.http.HttpServletRequest` por `jakarta.servlet.http.HttpServletRequest`:

| Arquivo | Import atual | Import alvo |
|---|---|---|
| `resources/exceptions/ResourceExceptionHandler.java` | `import javax.servlet.http.HttpServletRequest;` | `import jakarta.servlet.http.HttpServletRequest;` |

#### 2.2 Verificação pós-migração

Após substituir todos os imports, executar:

```cmd
mvnw.cmd clean package -DskipTests
```

O build deve produzir `BUILD SUCCESS` sem nenhuma linha contendo `cannot find symbol` referenciando `javax.persistence`, `javax.validation` ou `javax.servlet`. Para confirmar que não restou nenhum import legado:

```cmd
grep -r "import javax\." src/main/java
grep -r "import javax\." src/test/java
```

Ambos os comandos devem retornar zero resultados.

---

### 3. `SwaggerConfig.java` — Reescrita para springdoc-openapi

A classe atual usa a API do Springfox (`@EnableSwagger2`, `Docket`, imports `springfox.*`), que é incompatível com Spring Boot 3+. A nova implementação usa o bean `OpenAPI` do springdoc-openapi, que é detectado automaticamente pelo autoconfigure do springdoc.

#### Estado atual

```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
            .paths(PathSelectors.any())
            .build();
    }
}
```

#### Estado alvo

```java
package com.github.marcelomachadoxd.carteiraclientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI carteiraClientesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CarteiraClientes API")
                        .description("API REST para corretores de imóveis gerenciarem carteira de clientes")
                        .version("1.0.0"));
    }
}
```

**Decisões de design:**
- Não é necessária nenhuma anotação de habilitação (`@EnableSwagger2` não existe no springdoc). O autoconfigure do springdoc detecta o bean `OpenAPI` automaticamente.
- O bean `OpenAPI` é a forma idiomática de customizar título, descrição e versão no springdoc 3.x.
- O path padrão do Swagger UI no springdoc é `/swagger-ui/index.html` (diferente do `/swagger-ui.html` do Springfox).
- Todos os `@RestController` são incluídos automaticamente pelo springdoc sem configuração adicional.

---

### 4. `application.properties` — Ajustes para Spring Boot 4

#### Problema: propriedades `security.oauth2.*`

O namespace `security.oauth2.*` pertencia ao módulo `spring-security-oauth2` do Spring Boot 2.x, que foi removido no Spring Boot 3. No Spring Boot 4, essas propriedades não existem e causam warnings de propriedades desconhecidas na inicialização.

Como o projeto não implementa OAuth2 de forma funcional (as propriedades são apenas placeholders com valores padrão), a solução é remover ou comentar essas linhas.

```properties
# ANTES
security.oauth2.client.client-id=${CLIENT_ID:carteiraClientes}
security.oauth2.client.client-secret=${CLIENT_SECRET:TCC}

jwt.secret=${JWT_SECRET:MY-JWT-SECRET}
jwt.duration=${JWT_DURATION:86400}

# DEPOIS — remover as linhas security.oauth2.* (não gerenciadas pelo Spring Boot 4)
# As propriedades jwt.* são custom e podem ser mantidas se usadas por alguma classe
jwt.secret=${JWT_SECRET:MY-JWT-SECRET}
jwt.duration=${JWT_DURATION:86400}
```

**Decisão:** As propriedades `jwt.*` são custom (não pertencem a nenhum namespace do Spring Boot) e não causam warnings. Devem ser mantidas se alguma classe as injeta via `@Value`. Se não houver uso, podem ser removidas também.

#### Propriedades mantidas sem alteração

```properties
spring.profiles.active=${APP_PROFILE:test}
spring.jpa.open-in-view=false
```

Ambas são válidas no Spring Boot 4.

---

### 5. `application-test.properties` — Compatibilidade com Spring Boot 4

#### Análise das propriedades existentes

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=LEGACY
spring.datasource.username=sa
spring.datasource.password=

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Todas as propriedades acima são válidas no Spring Boot 4.** O namespace `spring.datasource.*`, `spring.h2.console.*` e `spring.jpa.*` não foi alterado. O parâmetro `MODE=LEGACY` na URL do H2 é uma opção do driver H2 (não do Spring Boot) e continua funcionando com a versão do H2 gerenciada pelo BOM 4.

**Nenhuma alteração é necessária** neste arquivo, desde que o H2 gerenciado pelo BOM 4 continue suportando `MODE=LEGACY` (o que é o caso nas versões H2 2.x).

---

### 6. Compatibilidade dos testes com Spring Boot 4

#### 6.1 Testes `@DataJpaTest` com jqwik

O padrão `ApplicationContextAware` + `TestContextManager` usado em `ClienteRepositoryTest` é compatível com Spring Boot 4. As APIs `@DataJpaTest`, `@AutoConfigureTestDatabase`, `@ActiveProfiles`, `ApplicationContextAware` e `TestContextManager` não foram alteradas no Spring Boot 4.

**Nenhuma alteração é necessária** nas classes de teste de repositório que seguem o padrão `ApplicationContextAware`, desde que o jqwik seja atualizado para 1.9.3.

#### 6.2 Testes `@SpringBootTest` com `MockMvc`

A classe `ClienteResourceTest` usa `@SpringBootTest` + `@AutoConfigureMockMvc` + `@SpyBean`. O `MockMvc` e o `ObjectMapper` continuam funcionando da mesma forma no Spring Boot 4.

**Ponto de atenção — Jackson 3:** O Spring Boot 4 adota Jackson 3 como serializador padrão. Para o padrão de uso atual (serialização/deserialização de DTOs simples com getters/setters e construtor padrão), não há incompatibilidade esperada. Ver seção 7 para análise detalhada.

#### 6.3 `@SpyBean` → `@MockitoSpyBean` (Spring Boot 4)

**Atenção:** O Spring Boot 4 removeu `@SpyBean` (do pacote `org.springframework.boot.test.mock.mockito`) em favor de `@MockitoSpyBean` (do pacote `org.springframework.test.context.bean.override.mockito`), alinhando-se com o Spring Framework 7.

A classe `ClienteResourceTest` usa `@SpyBean` na linha:

```java
@SpyBean
private ClienteService clienteService;
```

Esta anotação **não compilará** no Spring Boot 4. A correção é substituir pelo equivalente:

```java
// ANTES (Spring Boot 3)
import org.springframework.boot.test.mock.mockito.SpyBean;
@SpyBean
private ClienteService clienteService;

// DEPOIS (Spring Boot 4)
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
@MockitoSpyBean
private ClienteService clienteService;
```

O comportamento é idêntico: o `@MockitoSpyBean` envolve o bean real do contexto Spring com um spy do Mockito, permitindo que o teste 11 (`unmappedRuntimeException_shouldReturn500`) continue usando `Mockito.doThrow()` para simular exceções não mapeadas.

#### 6.4 `ClienteRepositoryPropertyTest` — Migração do padrão `@BeforeProperty`

A classe `ClienteRepositoryPropertyTest` usa `@BeforeProperty` + `TestContextManager` para inicializar o contexto Spring antes de cada propriedade jqwik. Este padrão pode apresentar problemas no Spring Boot 4 dependendo da versão do jqwik e da integração com o JUnit Platform.

O padrão recomendado pelo steering file `testing.md` é `ApplicationContextAware` + `TestContextManager` via `getRepository()`. A classe `ClienteRepositoryPropertyTest` deve ser migrada para este padrão:

```java
// ANTES — padrão @BeforeProperty (pode falhar no Spring Boot 4)
@BeforeProperty
void injectSpringDependencies() throws Exception {
    TestContextManager testContextManager = new TestContextManager(getClass());
    testContextManager.prepareTestInstance(this);
}

@Property(tries = 100)
void someProperty(@ForAll ...) {
    clienteRepository.findAll(); // usa campo @Autowired diretamente
}

// DEPOIS — padrão ApplicationContextAware (compatível com Spring Boot 4)
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

    @Property(tries = 100)
    void someProperty(@ForAll ...) {
        ClienteRepository repo = getRepository(); // SEMPRE via getRepository() em @Property
        repo.findAll();
    }
}
```

Todas as propriedades (`@Property`) e geradores (`@ForAll`) existentes devem ser preservados sem alteração de lógica.

#### 6.5 Testes de serviço com Mockito

As classes `ClienteServiceTest`, `RoleServiceTest`, `UserServiceTest` e `VisitaServiceTest` usam Mockito via `spring-boot-starter-test`. O Mockito é gerenciado pelo BOM do Spring Boot 4 e não requer versão explícita. Nenhuma alteração é necessária.

---

### 7. Compatibilidade com Jackson 3

O Spring Boot 4 adota Jackson 3 (tools.jackson) como serializador JSON padrão, gerenciado integralmente pelo BOM 4.0.0. Esta seção analisa o impacto no projeto.

#### 7.1 Gerenciamento de versão

Nenhuma dependência Jackson deve ser declarada explicitamente no `pom.xml`. O BOM do Spring Boot 4 gerencia a versão correta do Jackson 3 automaticamente. Se houver qualquer entrada `<dependency>` com `groupId` `com.fasterxml.jackson` ou `tools.jackson` no `pom.xml`, ela deve ser removida.

O projeto atual não declara dependências Jackson explícitas — os starters `spring-boot-starter-web` e `spring-boot-starter-test` já incluem Jackson transitivamente. Nenhuma alteração é necessária no `pom.xml` para este ponto.

#### 7.2 Análise de compatibilidade dos DTOs

O Jackson 3 mantém compatibilidade com o padrão JavaBean (getters/setters + construtor padrão) para serialização e desserialização. Todos os DTOs do projeto seguem este padrão:

| DTO | Construtor padrão | Getters/Setters | Compatível com Jackson 3 |
|---|---|---|---|
| `ClienteDTO` | ✓ | ✓ | ✓ |
| `UserDTO` | ✓ | ✓ | ✓ |
| `UserInsertDTO` | ✓ | ✓ | ✓ |
| `VisitaDTO` | ✓ | ✓ | ✓ |
| `ClienteDadosBasicosDTO` | ✓ | ✓ | ✓ |
| `RoleDTO` | verificar | verificar | verificar |

**Risco de `InvalidDefinitionException`:** O Jackson 3 lança `InvalidDefinitionException` quando tenta serializar/desserializar uma classe sem construtor padrão e sem anotações `@JsonCreator`. Como todos os DTOs acima possuem construtor padrão, este erro não é esperado. Se ocorrer, a causa raiz será um DTO sem construtor padrão — a solução é adicionar `public XxxDTO() {}`.

#### 7.3 Imports `com.fasterxml.jackson.*` no código-fonte

O `ObjectMapper` **não foi movido** no Jackson 3 — continua em `com.fasterxml.jackson.databind.ObjectMapper`. A classe `ClienteResourceTest` importa `ObjectMapper` diretamente:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
```

Este import **permanece válido** no Jackson 3. Nenhuma atualização de import é necessária para o código atual do projeto.

**Verificação:** Para confirmar que não há imports Jackson que precisem ser atualizados:

```cmd
grep -r "import com.fasterxml.jackson" src/
```

O único resultado esperado é o import de `ObjectMapper` em `ClienteResourceTest`, que é compatível com Jackson 3.

---

### 8. Steering files — Atualizações

#### 8.1 `.kiro/steering/tech.md`

Deve ser atualizado para refletir:
- Java 25 (era 11)
- Spring Boot 4.0.0 (era 2.4.5 parent + 3.0.0 starters)
- springdoc-openapi 3.0.3 (substituindo springfox 2.9.2)
- jqwik 1.9.3 (era 1.7.4)
- H2 gerenciado pelo BOM (era 2.1.214 explícito)
- JaCoCo 0.8.13 (era 0.8.8)
- Swagger UI path: `/swagger-ui/index.html` (era `/swagger-ui.html`)
- Remoção da nota de inconsistência entre parent BOM e starters
- Remoção de `hibernate-entitymanager` da tabela de dependências

#### 8.2 `.kiro/steering/migration-spring-boot-4.md` (novo)

Arquivo criado durante a migração para registrar decisões, problemas e soluções. Deve conter obrigatoriamente as seguintes seções, cada uma com ao menos uma entrada:

- **Substituições Realizadas** — lista de dependências removidas e adicionadas com versões
- **Imports Migrados** — lista de pacotes `javax.*` substituídos por `jakarta.*` com os arquivos afetados; exemplo de entrada:
  - `javax.persistence.*` → `jakarta.persistence.*` em `Cliente.java`, `Visita.java`, `User.java`, `Role.java`
  - `javax.validation.*` → `jakarta.validation.*` em `ClienteDTO.java`, `UserDTO.java`, `UserInsertDTO.java`, `VisitaDTO.java`
  - `javax.servlet.http.HttpServletRequest` → `jakarta.servlet.http.HttpServletRequest` em `ResourceExceptionHandler.java`
- **Problemas Encontrados e Soluções** — cada problema com descrição, causa raiz e solução aplicada
- **Propriedades Alteradas** — chaves renomeadas ou removidas com os valores antigos e novos
- **Status da Migração** — preenchida ao final com `Migração concluída com sucesso` e data no formato `YYYY-MM-DD`

---

## Modelos de Dados

Nenhum modelo de dados é alterado pela migração. As entidades JPA (`Cliente`, `Visita`, `User`, `Role`) e seus mapeamentos (`@Entity`, `@Table`, `@Column`, `@OneToMany`, `@ManyToOne`, `@ManyToMany`) são compatíveis com o Hibernate 7 gerenciado pelo Spring Boot 4, pois as anotações JPA são as mesmas — apenas o pacote muda de `javax.persistence.*` para `jakarta.persistence.*`.

**Estado atual dos imports nas entidades:** Todos os quatro arquivos de entidade (`Cliente.java`, `Visita.java`, `User.java`, `Role.java`) ainda usam `import javax.persistence.*;`. A migração para `import jakarta.persistence.*;` é coberta pela Etapa 2b da estratégia incremental (seção 2 deste documento).

---

## Propriedades de Corretude

*Uma propriedade é uma característica ou comportamento que deve ser verdadeiro em todas as execuções válidas de um sistema — essencialmente, uma declaração formal sobre o que o sistema deve fazer. Propriedades servem como ponte entre especificações legíveis por humanos e garantias de corretude verificáveis por máquina.*

Esta feature é uma migração de stack. A maioria dos critérios de aceitação são verificações de configuração (SMOKE) ou testes de integração de build (INTEGRATION), que não se beneficiam de property-based testing. No entanto, os critérios do Requisito 7 — que garantem a preservação do comportamento da API após a migração — contêm propriedades universais testáveis.

### Reflexão sobre redundância

Após análise do prework:

- Os critérios 7.2 e 7.3 descrevem comportamentos que variam com o input (IDs e payloads aleatórios) e são adequados para PBT.
- O critério 7.6 descreve o mapeamento de exceções para HTTP, que também varia com o tipo/mensagem da exceção.
- Os critérios 7.2 (GET por ID) e 7.6 (mapeamento de exceções) têm sobreposição parcial: quando um ID inexistente é buscado, `ResourceNotFoundException` é lançada e mapeada para 404. Porém, 7.2 testa o endpoint completo (incluindo serialização do DTO), enquanto 7.6 testa o mapeamento de exceções de forma isolada. São complementares, não redundantes.
- Os critérios 7.2 (404 para ID inexistente) e 7.6 (ResourceNotFoundException → 404) podem ser consolidados em uma única propriedade que verifica o comportamento end-to-end do endpoint para IDs inexistentes.

### Propriedade 1: Busca por ID existente retorna DTO completo

*Para qualquer* `Cliente` salvo no banco com campos preenchidos, uma requisição `GET /clientes/id/{id}` com o ID desse cliente deve retornar HTTP 200 com um corpo JSON contendo os campos `id`, `nome`, `email` e os campos de interesse (`qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`).

**Valida: Requisito 7.2**

### Propriedade 2: Busca por ID inexistente retorna 404

*Para qualquer* ID que não corresponda a nenhum `Cliente` no banco, uma requisição `GET /clientes/id/{id}` deve retornar HTTP 404 com um corpo JSON contendo o campo `error`.

**Valida: Requisitos 7.2, 7.6**

### Propriedade 3: Criação com payload válido retorna 201 com Location

*Para qualquer* `ClienteDTO` com `nome` não vazio, `email` válido e campos numéricos não negativos, uma requisição `POST /clientes` deve retornar HTTP 201 com header `Location` apontando para o recurso criado.

**Valida: Requisito 7.3**

### Propriedade 4: Criação com email inválido retorna 422 com erros de validação

*Para qualquer* string que não seja um endereço de email válido (sem `@`, com espaços, vazia), uma requisição `POST /clientes` com esse valor no campo `email` deve retornar HTTP 422 com um corpo JSON contendo o campo `errors` com ao menos um elemento cujo `fieldName` seja `"email"`.

**Valida: Requisito 7.3**

### Propriedade 5: Atualização com payload válido retorna 200

*Para qualquer* `Cliente` salvo no banco e qualquer `ClienteDTO` com campos válidos, uma requisição `PUT /clientes/id/{id}` com o ID desse cliente deve retornar HTTP 200 sem corpo.

**Valida: Requisito 9.6**

### Propriedade 6: Mapeamento de exceções para HTTP preservado

*Para qualquer* exceção `ResourceNotFoundException` lançada por um service, o `ResourceExceptionHandler` deve mapeá-la para HTTP 404. *Para qualquer* exceção `DatabaseException`, deve mapear para HTTP 400. *Para qualquer* exceção `MethodArgumentNotValidException`, deve mapear para HTTP 422 com corpo `ValidationError` contendo lista de `FieldMessage`.

**Valida: Requisito 9.10**

---

## Tratamento de Erros

### Erros esperados durante a migração

| Situação | Causa | Solução |
|---|---|---|
| `ClassNotFoundException: org.hibernate.ejb.HibernatePersistence` | `hibernate-entitymanager` removido mas ainda referenciado | Verificar se alguma classe importa `org.hibernate.ejb.*`; substituir por `jakarta.persistence.*` |
| `NoClassDefFoundError: springfox/...` | Springfox removido mas `SwaggerConfig` ainda importa `springfox.*` | Reescrever `SwaggerConfig` antes de remover as dependências, ou remover ambos simultaneamente |
| `WARN: Unknown property 'security.oauth2.client.client-id'` | Propriedade legada não reconhecida pelo Spring Boot 4 | Remover as linhas `security.oauth2.*` do `application.properties` |
| `UnsupportedClassVersionError` ao rodar testes | JaCoCo 0.8.8 não suporta bytecode Java 25 | Atualizar JaCoCo para 0.8.13 |
| Testes jqwik falham com `ClassNotFoundException` | jqwik 1.7.4 compilado para Java 11, incompatível com Java 25 | Atualizar jqwik para 1.9.3 |
| `404` em `/swagger-ui.html` | Path mudou no springdoc | Usar `/swagger-ui/index.html` |
| `javax.persistence.*` não encontrado | Entidades ainda usam namespace legado | Atualizar imports para `jakarta.persistence.*` |
| `@SpyBean` não encontrado / `ClassNotFoundException` | Spring Boot 4 removeu `@SpyBean` do módulo `spring-boot-test` | Substituir `@SpyBean` por `@MockitoSpyBean` (import: `org.springframework.test.context.bean.override.mockito.MockitoSpyBean`) em `ClienteResourceTest` |
| `InvalidDefinitionException` na serialização | Jackson 3 mudou comportamento de DTOs sem construtor padrão | Verificar que todos os DTOs possuem construtor padrão `public XxxDTO() {}`; adicionar se ausente |

### Comportamento de erro preservado

O `ResourceExceptionHandler` (`@ControllerAdvice`) e as exceções `ResourceNotFoundException` e `DatabaseException` não são alterados. O mapeamento HTTP é preservado:

- `ResourceNotFoundException` → HTTP 404 com corpo `StandardError`
- `DatabaseException` → HTTP 400 com corpo `StandardError`
- `MethodArgumentNotValidException` → HTTP 422 com corpo `ValidationError` contendo lista de `FieldMessage`
- `RuntimeException` (não mapeada) → HTTP 500 com corpo `StandardError`

O único arquivo que requer alteração nesta camada é `ResourceExceptionHandler.java`, exclusivamente para substituir o import `javax.servlet.http.HttpServletRequest` por `jakarta.servlet.http.HttpServletRequest` (Etapa 2b). A lógica dos handlers não é modificada.

---

## Estratégia de Testes

### Abordagem dual

A estratégia combina testes de integração (para verificar o comportamento do build e da aplicação em execução) com property-based tests (para verificar propriedades universais da API após a migração).

### Testes de integração de build (INTEGRATION / SMOKE)

Estes testes verificam que cada etapa da migração foi executada corretamente:

| Verificação | Comando | Critério de sucesso |
|---|---|---|
| Build sem testes | `mvnw.cmd clean package -DskipTests` | `BUILD SUCCESS`, sem `ERROR` |
| Todos os testes passam | `mvnw.cmd test` | `BUILD SUCCESS`, zero `FAILED` ou `ERROR` |
| Cobertura JaCoCo | `mvnw.cmd verify` | `BUILD SUCCESS`, cobertura LINE ≥ 70% |
| Swagger UI acessível | GET `/swagger-ui/index.html` | HTTP 200 |
| H2 Console acessível | GET `/h2-console` | HTTP 200 |
| Seed data carregado | GET `/clientes` | `totalElements` ≥ 1 |

### Property-based tests (jqwik)

Os testes de propriedade existentes (`ClienteRepositoryTest`, `ClienteRepositoryPropertyTest`) devem continuar passando sem alteração após a migração. Novos property-based tests para as Propriedades 1–4 devem ser implementados na classe `ClienteResourceTest` ou em uma nova classe `ClienteResourcePropertyTest`.

**Configuração dos property tests:**
- Biblioteca: jqwik 1.9.3 (já presente no projeto)
- Mínimo de iterações: 100 por propriedade (`@Property(tries = 100)`)
- Tag de rastreabilidade: comentário `// Feature: spring-boot-4-java-upgrade, Property N: <texto>`
- Padrão de integração com Spring: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")` + `@Transactional`

**Exemplo de estrutura para Propriedade 1:**

```java
// Feature: spring-boot-4-java-upgrade, Property 1: Busca por ID existente retorna DTO completo
@Property(tries = 100)
void findById_existingId_returns200WithAllFields(
        @ForAll @AlphaChars @StringLength(min = 1, max = 20) String nome,
        @ForAll @IntRange(min = 0, max = 10) int qtdQuartos) {
    // salvar cliente com dados gerados
    // GET /clientes/id/{id}
    // verificar HTTP 200 + campos no JSON
}
```

### Testes de unidade existentes

Os testes de unidade existentes (`ClienteServiceTest`, `RoleServiceTest`, etc.) não requerem alteração, pois usam Mockito e não dependem de APIs removidas no Spring Boot 4.

### Cobertura JaCoCo

A meta de 70% de cobertura de linhas é mantida. As classes excluídas continuam sendo `CarteiraClientesApplication` e `config/SwaggerConfig`. A configuração do plugin JaCoCo no `pom.xml` é preservada, apenas com a versão atualizada para 0.8.13.
