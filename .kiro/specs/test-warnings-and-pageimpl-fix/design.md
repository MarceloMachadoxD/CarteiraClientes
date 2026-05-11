# test-warnings-and-pageimpl-fix — Design do Bugfix

## Overview

Este documento formaliza a abordagem de correção para três warnings que ocorrem no projeto CarteiraClientes. Os dois primeiros (Bug 1 e Bug 2) são causados pelo Mockito e pelo Byte Buddy realizando self-attach dinâmico à JVM durante a execução dos testes, o que gera avisos de compatibilidade com versões futuras do Java. O terceiro (Bug 3) é causado pela serialização direta de instâncias `PageImpl` como resposta JSON nos endpoints paginados, cujo contrato de estrutura não é garantido pelo Spring entre versões.

A estratégia de correção é cirúrgica e não altera comportamento funcional:

- **Bug 1 e 2**: declarar `mockito-core` como `-javaagent` na `<argLine>` do `maven-surefire-plugin` no `pom.xml`, eliminando o self-attach dinâmico.
- **Bug 3**: criar o DTO `PageResponse<T>` no pacote `dto` e substituir o tipo de retorno `Page<DTO>` por `PageResponse<DTO>` em todos os resources paginados (`ClienteResource`, `VisitaResource`, `UserResource`). `RoleResource` não é afetado pois retorna `List<RoleDTO>`.

---

## Glossary

- **Bug_Condition (C)**: A condição que identifica entradas ou estados que disparam o bug — ausência de `-javaagent` declarado para o Mockito (Bugs 1 e 2) ou tipo de retorno `PageImpl` em endpoint paginado (Bug 3).
- **Property (P)**: O comportamento correto esperado quando a condição do bug é satisfeita — ausência dos warnings no console/log após a correção.
- **Preservation**: Comportamentos existentes que não devem ser alterados pela correção — testes continuam passando, endpoints não paginados continuam inalterados, dados retornados pelos endpoints paginados permanecem os mesmos.
- **`isBugCondition`**: Função pseudocódigo que retorna `true` quando a entrada satisfaz a condição do bug.
- **`PageImpl`**: Implementação concreta de `Page<T>` do Spring Data, cuja serialização JSON direta não tem estrutura garantida entre versões.
- **`PageResponse<T>`**: DTO genérico criado neste bugfix para encapsular os dados de paginação com estrutura JSON estável e explícita.
- **`maven-surefire-plugin`**: Plugin Maven responsável por executar os testes; sua `<argLine>` controla os argumentos passados à JVM de testes.
- **`-javaagent`**: Flag da JVM que carrega um agente Java antes da execução, evitando o self-attach dinâmico do Mockito.
- **`self-attach`**: Mecanismo pelo qual o Mockito tenta se registrar como agente Java em tempo de execução, depreciado nas versões recentes do JDK.

---

## Bug Details

### Bug 1 e 2 — Self-Attach do Mockito e Byte Buddy

O bug se manifesta quando os testes são executados com `@MockitoBean` ou mocks de classes concretas e o `mockito-core` não está declarado como `-javaagent` na `<argLine>` do `maven-surefire-plugin`. O Mockito tenta se auto-anexar à JVM dinamicamente, o que aciona o Byte Buddy pelo mesmo mecanismo, gerando dois warnings no console do Surefire.

**Especificação Formal:**

```
FUNCTION isBugCondition(X)
  INPUT: X de tipo TestExecution
  OUTPUT: boolean

  RETURN (X.usaMockitoBean = true OR X.mockClasseConcreta = true)
         AND NOT X.agenteJavaDeclarado = true
END FUNCTION
```

**Exemplos:**

- Teste com `@MockitoBean ClienteService clienteService` sem `-javaagent` → warning `"Mockito is currently self-attaching to enable the inline-mock-maker"` aparece no console do Surefire.
- Mesmo teste com `mockito-core` declarado como `-javaagent` na `<argLine>` → nenhum warning de self-attach.
- Teste sem nenhum mock (ex: `@DataJpaTest` puro) → condição do bug não se aplica, nenhum warning esperado.

---

### Bug 3 — Serialização Direta de `PageImpl`

O bug se manifesta quando um endpoint paginado retorna `ResponseEntity<Page<DTO>>` e o Spring serializa a instância `PageImpl` diretamente para JSON, emitindo o warning de instabilidade de estrutura no log da aplicação.

**Especificação Formal:**

```
FUNCTION isBugCondition(X)
  INPUT: X de tipo EndpointResponse
  OUTPUT: boolean

  // Retorna true quando o tipo de retorno do endpoint é Page<DTO>
  // serializado diretamente como PageImpl
  RETURN X.tipoRetorno = PageImpl
         AND X.endpoint IN {
           "GET /clientes",
           "GET /clientes/nome/{nome}",
           "GET /visitas",
           "GET /visitas/responsavel/{id}",
           "GET /visitas/cliente/{id}",
           "GET /users"
         }
END FUNCTION
```

**Exemplos:**

- `GET /clientes?margem=0` com `ClienteResource` retornando `ResponseEntity<Page<ClienteDTO>>` → warning `"Serializing PageImpl instances as-is is not supported"` no log.
- `GET /clientes/nome/Cliente` com `ClienteResource` retornando `ResponseEntity<Page<ClienteDTO>>` → mesmo warning.
- `GET /clientes/id/1` retornando `ResponseEntity<ClienteDTO>` → condição do bug não se aplica (endpoint não paginado).
- `GET /roles` retornando `ResponseEntity<List<RoleDTO>>` → condição do bug não se aplica (endpoint não paginado).
- `GET /clientes` com `ClienteResource` retornando `ResponseEntity<PageResponse<ClienteDTO>>` após a correção → nenhum warning, JSON com campos `content`, `totalElements`, `totalPages`, `number`, `size`, `first`, `last`.

---

## Expected Behavior

### Requisitos de Preservação

**Comportamentos que devem permanecer inalterados:**

- `mvnw.cmd test` deve continuar apresentando zero falhas (`failures=0`) e zero erros (`errors=0`) com o mesmo número de testes que passavam antes da correção.
- `GET /clientes/id/{id}` deve continuar retornando `200 OK` com `ClienteDTO` no corpo (endpoint não paginado, não afetado).
- `GET /roles` deve continuar retornando `200 OK` com `List<RoleDTO>` (endpoint não paginado, não afetado).
- `POST /clientes`, `PUT /clientes/id/{id}` e `DELETE /clientes/id/{id}` devem continuar funcionando com os mesmos status HTTP (`201 Created`, `200 OK`, `204 No Content`).
- A lógica de filtragem por interesses com margem percentual em `GET /clientes` deve permanecer inalterada.
- Os dados retornados nos campos `content`, `totalElements`, `totalPages`, `number` e `size` dos endpoints paginados devem ser idênticos aos que eram retornados pelo `PageImpl` anterior.
- `mvnw.cmd verify` deve continuar atingindo o mínimo de 70% de cobertura de linhas exigido pelo JaCoCo.
- Quando não há registros que satisfaçam os critérios de busca, os endpoints paginados devem continuar retornando `200 OK` com `content` vazio, `totalElements=0`, `totalPages=0`, `first=true` e `last=true`.

**Escopo:**

Todos os endpoints não paginados e todas as operações de escrita (`POST`, `PUT`, `DELETE`) não são afetados por este bugfix. A correção do Bug 3 é restrita à camada de resource (controllers) e à criação do DTO `PageResponse<T>` — nenhuma alteração é feita nos services, repositories ou entidades.

---

## Hypothesized Root Cause

### Bug 1 e 2 — Self-Attach do Mockito/Byte Buddy

1. **Ausência de declaração explícita do agente**: O `pom.xml` não possui a configuração `<argLine>` no `maven-surefire-plugin` com `-javaagent` apontando para o JAR do `mockito-core`. Sem essa declaração, o Mockito tenta se registrar dinamicamente em tempo de execução via `Instrumentation.attach()`, mecanismo que está sendo depreciado nas versões recentes do JDK (Java 21+) e que no Java 25 emite o warning de `terminally deprecated method in java.lang.System`.

2. **Ausência do `maven-surefire-plugin` configurado explicitamente**: O `pom.xml` atual não declara o `maven-surefire-plugin` com configuração customizada, dependendo apenas do comportamento padrão herdado do `spring-boot-starter-parent`. Isso impede a passagem de argumentos JVM adicionais para a JVM de testes.

3. **Interação com JaCoCo**: O JaCoCo já usa `jacoco:prepare-agent` para injetar seu próprio agente via `${argLine}`. A solução deve concatenar o agente do Mockito à `<argLine>` existente do JaCoCo, não substituí-la, para não quebrar a cobertura de código.

### Bug 3 — Serialização de `PageImpl`

1. **Ausência de DTO de paginação dedicado**: Os resources retornam `ResponseEntity<Page<DTO>>` diretamente. O Spring serializa a implementação concreta `PageImpl`, que possui campos internos e estrutura não garantida como estável entre versões do framework.

2. **Falta de contrato explícito de serialização**: Sem um DTO próprio, o Jackson serializa todos os campos públicos do `PageImpl`, incluindo campos internos que podem mudar entre versões do Spring Data. A solução é criar `PageResponse<T>` com apenas os campos necessários e explicitamente mapeados.

---

## Correctness Properties

Property 1: Bug Condition — Ausência de Warnings de Self-Attach

_For any_ execução de testes onde `isBugCondition(X)` retorna `true` (ou seja, há uso de `@MockitoBean` ou mock de classe concreta e o agente não está declarado explicitamente), após a correção o sistema SHALL executar os testes sem emitir as strings `"Mockito is currently self-attaching"` e `"terminally deprecated method in java.lang.System"` no console do Surefire.

**Validates: Requirements 2.1, 2.2**

Property 2: Bug Condition — Estrutura JSON Estável nos Endpoints Paginados

_For any_ chamada a endpoint paginado onde `isBugCondition(X)` retorna `true` (ou seja, o endpoint está na lista de endpoints paginados afetados), após a correção o sistema SHALL retornar um JSON contendo os campos `content`, `totalElements`, `totalPages`, `number`, `size`, `first` e `last`, sem emitir o warning `"Serializing PageImpl instances as-is is not supported"` no log da aplicação.

**Validates: Requirements 2.3**

Property 3: Preservation — Testes e Comportamento Funcional Inalterados

_For any_ entrada onde `isBugCondition(X)` retorna `false` (testes sem mocks inline, endpoints não paginados, operações de escrita), o sistema corrigido SHALL produzir exatamente o mesmo resultado que o sistema original, preservando zero falhas nos testes, os mesmos dados retornados pelos endpoints e a cobertura JaCoCo ≥ 70%.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8**

---

## Fix Implementation

### Correção 1 e 2 — Declarar `mockito-core` como `-javaagent` no `pom.xml`

**Arquivo**: `pom.xml`

**Mudanças necessárias**:

1. **Adicionar `maven-surefire-plugin` com `<argLine>` customizada**: Declarar o plugin explicitamente na seção `<build><plugins>` com a configuração abaixo. O placeholder `${argLine}` preserva o agente do JaCoCo injetado pelo `jacoco:prepare-agent`. O `${settings.localRepository}` resolve o caminho do repositório Maven local de forma portável.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            ${argLine}
            -javaagent:${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar
        </argLine>
    </configuration>
</plugin>
```

2. **Verificar a propriedade `${mockito.version}`**: O `spring-boot-starter-parent` 4.0.0 gerencia a versão do Mockito via BOM. A propriedade `${mockito.version}` deve estar disponível. Caso não esteja, declarar explicitamente em `<properties>` com a versão gerenciada pelo BOM (verificar em `mvnw.cmd dependency:tree -Dincludes=org.mockito:mockito-core`).

---

### Correção 3 — Criar `PageResponse<T>` e atualizar os resources paginados

**Arquivo novo**: `src/main/java/com/github/marcelomachadoxd/carteiraclientes/dto/PageResponse.java`

**Conteúdo**:

```java
package com.github.marcelomachadoxd.carteiraclientes.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public class PageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;
    private boolean first;
    private boolean last;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.first = page.isFirst();
        this.last = page.isLast();
    }

    // getters (sem setters — DTO imutável após construção)
    public List<T> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
```

**Arquivos alterados**: `ClienteResource.java`, `VisitaResource.java`, `UserResource.java`

**Mudanças necessárias em cada resource**:

1. Substituir `import org.springframework.data.domain.Page;` por `import com.github.marcelomachadoxd.carteiraclientes.dto.PageResponse;` (manter `Pageable`).
2. Alterar o tipo de retorno de cada método paginado de `ResponseEntity<Page<XxxDTO>>` para `ResponseEntity<PageResponse<XxxDTO>>`.
3. Envolver o resultado do service com `new PageResponse<>(page)` antes de passar ao `ResponseEntity.ok().body(...)`.

**Exemplo para `ClienteResource`**:

```java
// Antes
@GetMapping("/nome/{nome}")
public ResponseEntity<Page<ClienteDTO>> findByNome(@PathVariable String nome, Pageable pageable) {
    Page<ClienteDTO> clienteDTO = clienteService.findClienteByNome(nome, pageable);
    return ResponseEntity.ok().body(clienteDTO);
}

// Depois
@GetMapping("/nome/{nome}")
public ResponseEntity<PageResponse<ClienteDTO>> findByNome(@PathVariable String nome, Pageable pageable) {
    Page<ClienteDTO> clienteDTO = clienteService.findClienteByNome(nome, pageable);
    return ResponseEntity.ok().body(new PageResponse<>(clienteDTO));
}
```

**Mapeamento completo de endpoints afetados**:

| Resource | Método | Endpoint |
|---|---|---|
| `ClienteResource` | `findByNome` | `GET /clientes/nome/{nome}` |
| `ClienteResource` | `findByInteresses` | `GET /clientes` |
| `VisitaResource` | `findByResponsavelId` | `GET /visitas/responsavel/{id}` |
| `VisitaResource` | `findByClienteId` (por cliente) | `GET /visitas/cliente/{id}` |
| `VisitaResource` | `findByClienteId` (geral) | `GET /visitas` |
| `UserResource` | `findAllPageable` | `GET /users` |

**Não afetados** (não alterar):

- `RoleResource.findAll` → retorna `List<RoleDTO>`, não paginado.
- `ClienteResource.findById` → retorna `ClienteDTO`, não paginado.
- `VisitaResource.findById` → retorna `VisitaDTO`, não paginado.
- `UserResource.findById` → retorna `UserDTO`, não paginado.

---

## Testing Strategy

### Abordagem de Validação

A estratégia segue duas fases: primeiro, confirmar que o bug existe no código não corrigido (exploratory checking); depois, verificar que a correção elimina o bug sem introduzir regressões (fix checking e preservation checking).

---

### Exploratory Bug Condition Checking

**Objetivo**: Evidenciar os bugs no código não corrigido, confirmando ou refutando a análise de causa raiz. Se refutada, será necessário re-hipotetizar.

**Bug 1 e 2 — Plano de teste**:

Executar `mvnw.cmd test` no código não corrigido e observar o console do Surefire. Os warnings esperados são:

```
WARNING: A terminally deprecated method in java.lang.System has been called
Mockito is currently self-attaching to enable the inline-mock-maker.
```

Se os warnings aparecerem, a hipótese de causa raiz (ausência de `-javaagent`) é confirmada.

**Casos de teste exploratórios — Bug 1 e 2**:

1. **Execução completa da suite**: `mvnw.cmd test` → observar presença dos warnings no console.
2. **Classe com `@MockitoBean`**: identificar qual classe de teste usa `@MockitoBean` e executá-la isoladamente com `mvnw.cmd test -Dtest=NomeDaClasse` → confirmar que o warning aparece nessa classe.

**Bug 3 — Plano de teste**:

Executar a aplicação com `mvnw.cmd spring-boot:run` e chamar um endpoint paginado (ex: `GET /clientes`). Observar o log da aplicação. O warning esperado é:

```
Serializing PageImpl instances as-is is not supported, meaning that there is no guarantee
about the stability of the resulting JSON structure
```

Alternativamente, executar os testes de integração existentes e verificar se o warning aparece no log de teste.

**Casos de teste exploratórios — Bug 3**:

1. **`GET /clientes`**: chamar o endpoint e observar o log → confirmar warning de `PageImpl`.
2. **`GET /users`**: chamar o endpoint e observar o log → confirmar warning de `PageImpl`.
3. **`GET /roles`**: chamar o endpoint e observar o log → confirmar ausência do warning (não paginado).

---

### Fix Checking

**Objetivo**: Verificar que, para todas as entradas onde `isBugCondition(X)` retorna `true`, o sistema corrigido produz o comportamento esperado.

**Pseudocódigo:**

```
FOR ALL X WHERE isBugCondition(X) DO
  resultado ← executarCorrigido(X)
  ASSERT NOT contémWarning(resultado, "self-attaching")          // Bug 1
  ASSERT NOT contémWarning(resultado, "terminally deprecated")   // Bug 2
  ASSERT resultado.json contém "content"                         // Bug 3
  ASSERT resultado.json contém "totalElements"                   // Bug 3
  ASSERT resultado.json contém "totalPages"                      // Bug 3
  ASSERT resultado.json contém "number"                          // Bug 3
  ASSERT resultado.json contém "size"                            // Bug 3
  ASSERT resultado.json contém "first"                           // Bug 3
  ASSERT resultado.json contém "last"                            // Bug 3
  ASSERT NOT contémWarning(log, "Serializing PageImpl")          // Bug 3
END FOR
```

**Casos de teste de fix checking**:

1. **Ausência de warning de self-attach**: após adicionar `-javaagent` ao `pom.xml`, executar `mvnw.cmd test` e verificar que nenhum dos dois warnings aparece no console.
2. **Estrutura JSON de `GET /clientes`**: após criar `PageResponse<T>` e atualizar `ClienteResource`, chamar `GET /clientes` e verificar que o JSON contém os 7 campos esperados.
3. **Estrutura JSON de `GET /visitas`**: verificar os 7 campos em `GET /visitas`.
4. **Estrutura JSON de `GET /users`**: verificar os 7 campos em `GET /users`.
5. **Ausência de warning de `PageImpl`**: verificar que o warning não aparece no log após a correção.

---

### Preservation Checking

**Objetivo**: Verificar que, para todas as entradas onde `isBugCondition(X)` retorna `false`, o sistema corrigido produz o mesmo resultado que o sistema original.

**Pseudocódigo:**

```
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT executarOriginal(X) = executarCorrigido(X)
END FOR
```

**Abordagem**: Property-based testing é recomendado para preservation checking porque:
- Gera automaticamente muitos casos de teste sobre o domínio de entrada.
- Captura edge cases que testes unitários manuais podem não cobrir.
- Fornece garantias fortes de que o comportamento é preservado para todas as entradas não-bugadas.

**Plano**: Observar o comportamento no código não corrigido para entradas não-bugadas (endpoints não paginados, operações de escrita, testes sem mocks inline), depois escrever testes que capturam esse comportamento e verificam que ele se mantém após a correção.

**Casos de teste de preservation checking**:

1. **Preservação de `GET /clientes/id/{id}`**: verificar que o endpoint não paginado continua retornando `200 OK` com `ClienteDTO` correto após a correção.
2. **Preservação de `GET /roles`**: verificar que o endpoint não paginado continua retornando `200 OK` com `List<RoleDTO>`.
3. **Preservação dos dados paginados**: verificar que `content`, `totalElements`, `totalPages`, `number` e `size` retornados pelo `PageResponse<T>` são idênticos aos que seriam retornados pelo `PageImpl` para os mesmos parâmetros de paginação.
4. **Preservação de operações de escrita**: verificar que `POST /clientes`, `PUT /clientes/id/{id}` e `DELETE /clientes/id/{id}` continuam funcionando com os mesmos status HTTP.
5. **Preservação da suite de testes**: `mvnw.cmd test` deve continuar com zero falhas e zero erros.
6. **Preservação da cobertura JaCoCo**: `mvnw.cmd verify` deve continuar atingindo ≥ 70% de cobertura de linhas.

---

### Unit Tests

- Testar o construtor de `PageResponse<T>` com um `Page<T>` mockado, verificando que todos os 7 campos são corretamente mapeados.
- Testar `PageResponse<T>` com página vazia (`content` vazio, `totalElements=0`, `totalPages=0`, `first=true`, `last=true`).
- Testar `PageResponse<T>` com página intermediária (não primeira, não última) verificando `first=false` e `last=false`.
- Testar que `ClienteResource`, `VisitaResource` e `UserResource` retornam `PageResponse<T>` nos endpoints paginados (via `@WebMvcTest` ou `MockMvc`).

### Property-Based Tests

- Gerar aleatoriamente parâmetros de paginação (`page`, `size`) e verificar que `PageResponse<ClienteDTO>` retornado por `GET /clientes` sempre contém os 7 campos obrigatórios com valores consistentes (`number >= 0`, `size > 0`, `totalPages >= 0`, `totalElements >= 0`, `first == (number == 0)`, `last == (number == totalPages - 1 || totalPages == 0)`).
- Gerar aleatoriamente prefixos de nome e verificar que `GET /clientes/nome/{nome}` retorna `PageResponse<ClienteDTO>` com `content` contendo apenas clientes cujo nome começa com o prefixo fornecido (preservation da lógica de filtragem).
- Gerar aleatoriamente parâmetros de interesse e verificar que `GET /clientes` aplica corretamente a margem percentual sobre `valorMaximo` e `metragem` (preservation da lógica de filtragem por interesses).

### Integration Tests

- Testar o fluxo completo de `GET /clientes` com seed data: verificar que `content` contém os clientes esperados e que os metadados de paginação são corretos.
- Testar `GET /visitas/responsavel/2` com seed data (User id=2 é responsável pelas Visitas id=1..30): verificar que `totalElements=30` e `content` contém as visitas corretas.
- Testar `GET /users` com seed data: verificar estrutura `PageResponse<UserDTO>` e dados corretos.
- Testar que `GET /roles` continua retornando `List<RoleDTO>` (não `PageResponse`) após a correção.
- Testar `mvnw.cmd verify` completo para confirmar cobertura JaCoCo ≥ 70% com o novo `PageResponse<T>` incluído na cobertura.
