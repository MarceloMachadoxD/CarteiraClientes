# Documento de Requisitos de Bugfix

## Introduction

Este bugfix corrige três avisos (warnings) que aparecem durante a execução dos testes do projeto CarteiraClientes. Os dois primeiros estão relacionados ao carregamento dinâmico de agentes Java pelo Mockito e pelo Byte Buddy durante os testes, o que gera avisos de compatibilidade com versões futuras do Java. O terceiro aviso é emitido pelo Spring ao serializar `PageImpl` diretamente como resposta JSON nos endpoints de listagem paginada, cuja estrutura não é garantida como estável entre versões do framework. A correção elimina todos os avisos sem alterar o comportamento funcional dos testes nem das respostas da API.

## Bug Analysis

### Current Behavior (Defect)

1.1 QUANDO os testes são executados com `@MockitoBean` ou mocks de classes concretas ENTÃO o Mockito se auto-anexa à JVM como agente Java dinamicamente, emitindo o warning `"Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK."` no console de testes

1.2 QUANDO o Mockito realiza o self-attach descrito em 1.1 ENTÃO o Byte Buddy também é carregado dinamicamente como agente, emitindo o warning `"WARNING: A terminally deprecated method in java.lang.System has been called"` associado ao carregamento dinâmico de agentes

1.3 QUANDO um endpoint de listagem paginada (ex: `GET /clientes`, `GET /clientes/nome/{nome}`, `GET /visitas`, `GET /users`, `GET /roles`) é chamado ENTÃO o Spring serializa um `PageImpl` diretamente para JSON e emite o warning `"Serializing PageImpl instances as-is is not supported, meaning that there is no guarantee about the stability of the resulting JSON structure"` no log da aplicação

### Expected Behavior (Correct)

2.1 QUANDO os testes são executados com `@MockitoBean` ou mocks de classes concretas ENTÃO o sistema SHALL declarar o `mockito-core` como `-javaagent` na configuração `<argLine>` do `maven-surefire-plugin` no `pom.xml`, de modo que a string `"Mockito is currently self-attaching"` NÃO apareça no console do Surefire após `mvnw.cmd test`

2.2 QUANDO o `mockito-core` é declarado como `-javaagent` conforme 2.1 ENTÃO o sistema SHALL carregar o Byte Buddy como parte do mesmo agente declarado, de modo que a string `"terminally deprecated method in java.lang.System"` associada ao carregamento dinâmico de agentes NÃO apareça no console do Surefire após `mvnw.cmd test`

2.3 QUANDO um endpoint de listagem paginada retorna dados paginados ENTÃO o sistema SHALL serializar um DTO próprio `PageResponse<T>` em vez de `PageImpl`, onde:
- `content` é a lista de DTOs da página atual (tipo `List<T>`)
- `totalElements` é o número total de registros (tipo `long`)
- `totalPages` é o número total de páginas (tipo `int`)
- `number` é o índice da página atual, zero-indexed (tipo `int`)
- `size` é o tamanho da página solicitada (tipo `int`)
- `first` é `true` se esta é a primeira página (tipo `boolean`)
- `last` é `true` se esta é a última página (tipo `boolean`)
- A string `"Serializing PageImpl instances as-is is not supported"` NÃO aparece no log da aplicação

### Unchanged Behavior (Regression Prevention)

3.1 QUANDO `mvnw.cmd test` é executado após a correção ENTÃO o sistema SHALL CONTINUE TO apresentar zero falhas (`failures=0`) e zero erros (`errors=0`) no relatório do Surefire, com o mesmo número de testes que passavam antes da correção

3.2 QUANDO `GET /clientes/id/{id}` é chamado com um id existente ENTÃO o sistema SHALL CONTINUE TO retornar `200 OK` com o `ClienteDTO` correspondente no corpo (endpoint não paginado não é afetado pela introdução de `PageResponse<T>`)

3.3 QUANDO `GET /clientes/nome/{nome}` é chamado com um nome válido e parâmetros de paginação ENTÃO o sistema SHALL CONTINUE TO retornar `200 OK` com `PageResponse<ClienteDTO>` contendo os mesmos clientes que seriam retornados pelo `PageImpl` anterior, com `totalElements`, `totalPages`, `number` e `size` refletindo corretamente os parâmetros de paginação fornecidos

3.4 QUANDO `GET /clientes` é chamado com parâmetros de filtro por interesses e margem de tolerância ENTÃO o sistema SHALL CONTINUE TO aplicar a lógica de filtragem com margem percentual sobre `valorMaximo` e `metragem` (parâmetros com valor `0` ignorados) e retornar os resultados em `PageResponse<ClienteDTO>`

3.5 QUANDO `GET /visitas`, `GET /visitas/responsavel/{id}` e `GET /visitas/cliente/{id}` são chamados com parâmetros de paginação ENTÃO o sistema SHALL CONTINUE TO retornar os dados paginados em `PageResponse<VisitaDTO>`; QUANDO `GET /users` é chamado com parâmetros de paginação ENTÃO o sistema SHALL CONTINUE TO retornar `PageResponse<UserDTO>`; QUANDO `GET /roles` é chamado ENTÃO o sistema SHALL CONTINUE TO retornar `List<RoleDTO>` (endpoint não paginado, não afetado)

3.6 QUANDO `POST /clientes`, `PUT /clientes/id/{id}` e `DELETE /clientes/id/{id}` são chamados ENTÃO o sistema SHALL CONTINUE TO executar as operações de criação (retornando `201 Created`), atualização (retornando `200 OK`) e deleção (retornando `204 No Content`) sem alteração de comportamento

3.7 QUANDO a cobertura de código é verificada com `mvnw.cmd verify` ENTÃO o sistema SHALL CONTINUE TO atingir o mínimo de 70% de cobertura de linhas exigido pelo JaCoCo

3.8 QUANDO um endpoint de listagem paginada é chamado e não há registros que satisfaçam os critérios de busca ENTÃO o sistema SHALL CONTINUE TO retornar `200 OK` com `PageResponse<T>` onde `content` é lista vazia, `totalElements` é `0`, `totalPages` é `0`, `first` é `true` e `last` é `true`

---

## Condição do Bug e Propriedades

### Bug 1 e 2 — Agente Mockito/Byte Buddy

**Condição do bug:**
```pascal
FUNCTION isBugCondition(X)
  INPUT: X de tipo TestExecution
  OUTPUT: boolean

  // Retorna true quando o Mockito precisa criar mocks inline
  // e não há agente Java declarado explicitamente no surefire
  RETURN X.usaMockitoBean = true OR X.mockClasseConcreta = true
         AND NOT X.agenteJavaDeclarado = true
END FUNCTION
```

**Propriedade — Fix Checking:**
```pascal
// Propriedade: Verificação da Correção — Ausência de Warning de Self-Attach
FOR ALL X WHERE isBugCondition(X) DO
  resultado ← executarTestes'(X)
  ASSERT NOT contémWarning(resultado, "self-attaching")
         AND NOT contémWarning(resultado, "terminally deprecated")
END FOR
```

**Propriedade — Preservation Checking:**
```pascal
// Propriedade: Verificação de Preservação — Testes continuam passando
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT executarTestes(X) = executarTestes'(X)
END FOR
```

---

### Bug 3 — Serialização de `PageImpl`

**Condição do bug:**
```pascal
FUNCTION isBugCondition(X)
  INPUT: X de tipo EndpointResponse
  OUTPUT: boolean

  // Retorna true quando o tipo de retorno do endpoint é Page<DTO>
  // serializado diretamente como PageImpl
  RETURN X.tipoRetorno = PageImpl AND X.endpoint IN endpointsPaginados
END FUNCTION
```

**Propriedade — Fix Checking:**
```pascal
// Propriedade: Verificação da Correção — Estrutura JSON estável
FOR ALL X WHERE isBugCondition(X) DO
  resultado ← chamarEndpoint'(X)
  ASSERT resultado.json contém "content"
         AND resultado.json contém "totalElements"
         AND resultado.json contém "totalPages"
         AND resultado.json contém "number"
         AND resultado.json contém "size"
         AND resultado.json contém "first"
         AND resultado.json contém "last"
         AND NOT contémWarning(log, "Serializing PageImpl instances as-is is not supported")
END FOR
```

**Propriedade — Preservation Checking:**
```pascal
// Propriedade: Verificação de Preservação — Dados de conteúdo inalterados
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT chamarEndpoint(X) = chamarEndpoint'(X)
END FOR
```
