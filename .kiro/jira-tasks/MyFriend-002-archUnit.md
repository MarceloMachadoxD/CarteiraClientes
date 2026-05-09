# MyFriend-002 — Testes ArchUnit: Controllers livres de anotações Swagger

## Contexto

Esta task foi extraída do escopo da feature `swagger-enrichment` (MyFriend-001) e deve ser implementada em momento separado, após a conclusão das interfaces contratuais.

A feature `swagger-enrichment` garante por convenção que os controllers não possuem anotações Swagger diretamente — esta task adiciona a verificação automatizada dessa propriedade via ArchUnit.

---

## Requisitos Relacionados

Extraídos de `.kiro/specs/swagger-enrichment/requirements.md`:

### Requirement 1 — Critérios 1.5 a 1.8 (Separação de Responsabilidades)

5. WHEN uma regra ArchUnit verifica a classe `ClienteResource`, THE **ArchUnit_Rule** SHALL confirmar que nenhum método de `ClienteResource` possui anotações do pacote `io.swagger.v3.oas.annotations.*` diretamente declaradas nele.
6. WHEN uma regra ArchUnit verifica a classe `VisitaResource`, THE **ArchUnit_Rule** SHALL confirmar que nenhum método de `VisitaResource` possui anotações do pacote `io.swagger.v3.oas.annotations.*` diretamente declaradas nele.
7. WHEN uma regra ArchUnit verifica a classe `UserResource`, THE **ArchUnit_Rule** SHALL confirmar que nenhum método de `UserResource` possui anotações do pacote `io.swagger.v3.oas.annotations.*` diretamente declaradas nele.
8. WHEN uma regra ArchUnit verifica a classe `RoleResource`, THE **ArchUnit_Rule** SHALL confirmar que nenhum método de `RoleResource` possui anotações do pacote `io.swagger.v3.oas.annotations.*` diretamente declaradas nele.

### Requirement 2 — Critério 2.7 (Imports Jakarta)

7. THE **SwaggerConfig** SHALL usar imports exclusivamente dos pacotes `io.swagger.v3.oas.*`, `org.springframework.*` e `jakarta.*`, sendo proibido qualquer import do pacote `javax.*`.

---

## Propriedade Formal

**Propriedade 1: Controllers livres de anotações Swagger**

*Para qualquer* classe anotada com `@RestController` no pacote `resources` (excluindo o subpacote `documentation`), nenhum método declarado diretamente nessa classe deve possuir anotações do pacote `io.swagger.v3.oas.annotations.*`.

Esta propriedade é universal: deve valer para qualquer método presente na classe, independentemente de quantos métodos existam. Por isso é adequada para verificação via ArchUnit (verificação exaustiva sobre todos os métodos).

**Valida: Requisitos 1.5, 1.6, 1.7, 1.8**

---

## Design

Extraído de `.kiro/specs/swagger-enrichment/design.md` — seção "Estratégia de Testes":

### Dependência necessária no `pom.xml`

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

### Classe `SwaggerDocumentationArchTest`

**Localização:** `src/test/java/com/github/marcelomachadoxd/carteiraclientes/resources/SwaggerDocumentationArchTest.java`

```java
@AnalyzeClasses(packages = "com.github.marcelomachadoxd.carteiraclientes.resources")
class SwaggerDocumentationArchTest {

    // Propriedade 1: nenhum método de controller tem anotação Swagger diretamente
    @ArchTest
    static final ArchRule controllers_nao_devem_ter_anotacoes_swagger =
        methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .should().notBeAnnotatedWith(io.swagger.v3.oas.annotations.Operation.class)
            .andShould().notBeAnnotatedWith(io.swagger.v3.oas.annotations.Parameter.class)
            .andShould().notBeAnnotatedWith(io.swagger.v3.oas.annotations.responses.ApiResponse.class)
            .andShould().notBeAnnotatedWith(io.swagger.v3.oas.annotations.responses.ApiResponses.class)
            .because("Anotações Swagger devem residir exclusivamente nas interfaces contratuais");

    // Regra adicional: SwaggerConfig não deve importar javax.*
    @ArchTest
    static final ArchRule swagger_config_nao_usa_javax =
        noClasses()
            .that().haveSimpleName("SwaggerConfig")
            .should().dependOnClassesThat().resideInAPackage("javax..")
            .because("Spring Boot 4 usa jakarta.* em vez de javax.*");
}
```

### Mapeamento de regras para requisitos

| Regra ArchUnit | Requisitos validados |
|---|---|
| `controllers_nao_devem_ter_anotacoes_swagger` | 1.5, 1.6, 1.7, 1.8 |
| `swagger_config_nao_usa_javax` | 2.7 |

---

## Tasks

- [ ] 1. Adicionar dependência ArchUnit ao `pom.xml`
  - Inserir `<dependency>` de `com.tngtech.archunit:archunit-junit5:1.3.0` com `<scope>test</scope>`
  - Pré-requisito: feature `swagger-enrichment` concluída (interfaces contratuais implementadas e controllers sem anotações Swagger)
  - _Requisitos: 1.5, 1.6, 1.7, 1.8_

- [ ] 2. Criar `SwaggerDocumentationArchTest` com a regra `swagger_config_nao_usa_javax`
  - Criar a classe em `src/test/java/.../carteiraclientes/resources/SwaggerDocumentationArchTest.java`
  - Usar `@AnalyzeClasses(packages = "com.github.marcelomachadoxd.carteiraclientes.resources")`
  - Implementar `swagger_config_nao_usa_javax` verificando que `SwaggerConfig` não depende de classes em `javax..`
  - _Requisitos: 2.7_

- [ ] 3. Implementar a regra ArchUnit para a Propriedade 1: controllers livres de anotações Swagger
  - Implementar `controllers_nao_devem_ter_anotacoes_swagger`: para qualquer classe `@RestController` no pacote `resources` (excluindo `documentation`), nenhum método declarado diretamente deve ter anotações de `io.swagger.v3.oas.annotations.*`
  - Verificar `@Operation`, `@Parameter`, `@ApiResponse`, `@ApiResponses`
  - **Valida: Requisitos 1.5, 1.6, 1.7, 1.8**

- [ ] 4. Checkpoint — Executar `mvnw.cmd test` e verificar que as regras ArchUnit passam

## Notas

- Esta task depende que a feature `swagger-enrichment` esteja concluída: os controllers devem implementar as interfaces contratuais e não ter nenhuma anotação Swagger diretamente nos métodos
- A regra `controllers_nao_devem_ter_anotacoes_swagger` falhará se qualquer controller ainda tiver anotações `@Operation`, `@Parameter`, `@ApiResponse` ou `@ApiResponses` diretamente nos métodos
- Todos os imports devem usar `jakarta.*` — nunca `javax.*` (Spring Boot 4 + Java 25)
