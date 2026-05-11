# Plano de Implementação

- [x] 1. Escrever teste de exploração da condição do bug (Bugs 1 e 2 — Self-Attach)
  - **Property 1: Bug Condition** - Mockito/Byte Buddy Self-Attach Warning
  - **IMPORTANTE**: Escrever este teste ANTES de implementar a correção
  - **OBJETIVO**: Confirmar que os warnings de self-attach existem no código não corrigido
  - **Abordagem Scoped PBT**: Escopo determinístico — executar `mvnw.cmd test` no código não corrigido e observar o console do Surefire
  - Verificar que a string `"Mockito is currently self-attaching to enable the inline-mock-maker"` aparece no console após `mvnw.cmd test`
  - Verificar que a string `"WARNING: A terminally deprecated method in java.lang.System has been called"` aparece no console após `mvnw.cmd test`
  - Identificar qual classe de teste usa `@MockitoBean` e executá-la isoladamente com `mvnw.cmd test -Dtest=NomeDaClasse` para confirmar que o warning aparece nessa classe
  - Executar `mvnw.cmd test` no código não corrigido
  - **RESULTADO ESPERADO**: Warnings aparecem no console (confirma que o bug existe)
  - Documentar os counterexamples encontrados (ex: `"ClienteServiceTest com @MockitoBean ClienteService → warning de self-attach aparece"`)
  - Marcar a tarefa como concluída quando o teste for escrito, executado e a falha documentada
  - _Requirements: 1.1, 1.2_

- [x] 2. Escrever testes de preservação (ANTES de implementar a correção)
  - **Property 2: Preservation** - Suite de Testes Passa Sem Regressões
  - **IMPORTANTE**: Seguir a metodologia observation-first
  - Observar: executar `mvnw.cmd test` no código não corrigido e registrar o número de testes que passam (`failures=0`, `errors=0`)
  - Observar: executar `mvnw.cmd verify` e confirmar que a cobertura JaCoCo ≥ 70% está sendo atingida
  - Escrever testes de preservação que capturam o comportamento observado para entradas não-bugadas:
    - Testes de `GET /clientes/id/{id}` retornando `200 OK` com `ClienteDTO` correto (endpoint não paginado)
    - Testes de `GET /roles` retornando `200 OK` com `List<RoleDTO>` (endpoint não paginado)
    - Testes de `POST /clientes`, `PUT /clientes/id/{id}` e `DELETE /clientes/id/{id}` com os status HTTP corretos (`201 Created`, `200 OK`, `204 No Content`)
    - Testes de `GET /clientes` com seed data verificando que `content` contém os clientes esperados e metadados de paginação corretos
    - Testes de `GET /visitas/responsavel/2` com seed data verificando `totalElements=30` e `content` correto
  - Executar os testes de preservação no código não corrigido
  - **RESULTADO ESPERADO**: Testes PASSAM (confirma o comportamento de baseline a preservar)
  - Marcar a tarefa como concluída quando os testes forem escritos, executados e passando no código não corrigido
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

- [x] 3. Escrever teste de exploração da condição do bug (Bug 3 — Serialização de `PageImpl`)
  - **Property 1: Bug Condition** - PageImpl Serialization Warning
  - **IMPORTANTE**: Escrever este teste ANTES de implementar a correção do Bug 3
  - **OBJETIVO**: Confirmar que o warning de serialização de `PageImpl` existe no código não corrigido
  - **Abordagem Scoped PBT**: Escopo determinístico — chamar os endpoints paginados e observar o log da aplicação
  - Verificar que a string `"Serializing PageImpl instances as-is is not supported"` aparece no log ao chamar `GET /clientes`
  - Verificar que a string `"Serializing PageImpl instances as-is is not supported"` aparece no log ao chamar `GET /users`
  - Verificar que `GET /roles` NÃO emite o warning (endpoint não paginado — confirma que a condição do bug não se aplica)
  - Executar os testes de integração existentes e verificar se o warning aparece no log de teste
  - **RESULTADO ESPERADO**: Warning aparece nos endpoints paginados (confirma que o bug existe)
  - Documentar os counterexamples encontrados (ex: `"GET /clientes com ClienteResource retornando Page<ClienteDTO> → warning de PageImpl aparece no log"`)
  - Marcar a tarefa como concluída quando o teste for escrito, executado e a falha documentada
  - _Requirements: 1.3_

- [x] 4. Correção dos Bugs 1 e 2 — Declarar `mockito-core` como `-javaagent` no `pom.xml`

  - [x] 4.1 Verificar a versão do `mockito-core` gerenciada pelo BOM
    - Executar `mvnw.cmd dependency:tree -Dincludes=org.mockito:mockito-core` para obter a versão exata gerenciada pelo `spring-boot-starter-parent` 4.0.0
    - Anotar a versão encontrada (ex: `5.x.x`) para uso na `<argLine>`
    - _Requirements: 2.1, 2.2_

  - [x] 4.2 Adicionar `maven-surefire-plugin` com `<argLine>` customizada no `pom.xml`
    - Declarar o `maven-surefire-plugin` explicitamente na seção `<build><plugins>` do `pom.xml`
    - Configurar `<argLine>` concatenando `${argLine}` (agente JaCoCo) com `-javaagent` apontando para o JAR do `mockito-core` no repositório Maven local
    - Usar `${settings.localRepository}` para resolver o caminho do repositório de forma portável
    - Usar `${mockito.version}` para referenciar a versão gerenciada pelo BOM (ou declarar explicitamente em `<properties>` se necessário)
    - Garantir que `${argLine}` do JaCoCo é preservado (não substituído) para não quebrar a cobertura de código
    - _Bug_Condition: isBugCondition(X) onde X.usaMockitoBean = true OR X.mockClasseConcreta = true AND NOT X.agenteJavaDeclarado = true_
    - _Expected_Behavior: NOT contémWarning(resultado, "self-attaching") AND NOT contémWarning(resultado, "terminally deprecated")_
    - _Preservation: mvnw.cmd test continua com failures=0 e errors=0; mvnw.cmd verify continua com cobertura JaCoCo ≥ 70%_
    - _Requirements: 2.1, 2.2, 3.1, 3.7_

  - [x] 4.3 Verificar que o teste de exploração (Property 1 — Self-Attach) agora passa
    - **Property 1: Expected Behavior** - Ausência de Warnings de Self-Attach
    - **IMPORTANTE**: Re-executar o MESMO teste da tarefa 1 — NÃO escrever um novo teste
    - O teste da tarefa 1 codifica o comportamento esperado (ausência dos warnings)
    - Executar `mvnw.cmd test` e verificar que `"Mockito is currently self-attaching"` NÃO aparece no console
    - Executar `mvnw.cmd test` e verificar que `"terminally deprecated method in java.lang.System"` NÃO aparece no console
    - **RESULTADO ESPERADO**: Teste PASSA (confirma que os Bugs 1 e 2 estão corrigidos)
    - _Requirements: 2.1, 2.2_

  - [x] 4.4 Verificar que os testes de preservação ainda passam após a correção dos Bugs 1 e 2
    - **Property 2: Preservation** - Suite de Testes Passa Sem Regressões
    - **IMPORTANTE**: Re-executar os MESMOS testes da tarefa 2 — NÃO escrever novos testes
    - Executar `mvnw.cmd test` e confirmar `failures=0` e `errors=0` com o mesmo número de testes
    - Executar `mvnw.cmd verify` e confirmar cobertura JaCoCo ≥ 70%
    - **RESULTADO ESPERADO**: Testes PASSAM (confirma que não há regressões)

- [ ] 5. Correção do Bug 3 — Criar `PageResponse<T>` e atualizar os resources paginados

  - [~] 5.1 Criar o DTO `PageResponse<T>` no pacote `dto`
    - Criar o arquivo `src/main/java/com/github/marcelomachadoxd/carteiraclientes/dto/PageResponse.java`
    - Implementar o DTO genérico com os 7 campos obrigatórios: `content` (`List<T>`), `totalElements` (`long`), `totalPages` (`int`), `number` (`int`), `size` (`int`), `first` (`boolean`), `last` (`boolean`)
    - Implementar construtor `PageResponse(Page<T> page)` que mapeia todos os campos a partir do `Page<T>` recebido
    - Implementar apenas getters (sem setters — DTO imutável após construção)
    - Usar `import org.springframework.data.domain.Page` e `import java.util.List`
    - _Bug_Condition: isBugCondition(X) onde X.tipoRetorno = PageImpl AND X.endpoint IN endpointsPaginados_
    - _Expected_Behavior: resultado.json contém "content", "totalElements", "totalPages", "number", "size", "first", "last" AND NOT contémWarning(log, "Serializing PageImpl instances as-is is not supported")_
    - _Preservation: dados retornados em content, totalElements, totalPages, number e size são idênticos aos que seriam retornados pelo PageImpl para os mesmos parâmetros_
    - _Requirements: 2.3, 3.3, 3.4, 3.5, 3.8_

  - [~] 5.2 Atualizar `ClienteResource` para retornar `PageResponse<ClienteDTO>`
    - Substituir `import org.springframework.data.domain.Page` por `import com.github.marcelomachadoxd.carteiraclientes.dto.PageResponse` (manter `Pageable`)
    - Alterar o tipo de retorno de `findByNome` de `ResponseEntity<Page<ClienteDTO>>` para `ResponseEntity<PageResponse<ClienteDTO>>`
    - Alterar o tipo de retorno de `findByInteresses` de `ResponseEntity<Page<ClienteDTO>>` para `ResponseEntity<PageResponse<ClienteDTO>>`
    - Envolver o resultado do service com `new PageResponse<>(clienteDTO)` antes de passar ao `ResponseEntity.ok().body(...)`
    - Não alterar `findById`, `insert`, `delete` e `update` (endpoints não paginados)
    - _Requirements: 2.3, 3.3, 3.4_

  - [~] 5.3 Atualizar `VisitaResource` para retornar `PageResponse<VisitaDTO>`
    - Substituir `import org.springframework.data.domain.Page` por `import com.github.marcelomachadoxd.carteiraclientes.dto.PageResponse` (manter `Pageable`)
    - Alterar o tipo de retorno de `findByResponsavelId` de `ResponseEntity<Page<VisitaDTO>>` para `ResponseEntity<PageResponse<VisitaDTO>>`
    - Alterar o tipo de retorno de `findByClienteId` (path `/cliente/{id}`) de `ResponseEntity<Page<VisitaDTO>>` para `ResponseEntity<PageResponse<VisitaDTO>>`
    - Alterar o tipo de retorno de `findByClienteId` (path raiz `/`) de `ResponseEntity<Page<VisitaDTO>>` para `ResponseEntity<PageResponse<VisitaDTO>>`
    - Envolver o resultado do service com `new PageResponse<>(visitaDTO)` em cada método paginado
    - Não alterar `findById`, `delete` e `insert` (endpoints não paginados ou de escrita)
    - _Requirements: 2.3, 3.5_

  - [~] 5.4 Atualizar `UserResource` para retornar `PageResponse<UserDTO>`
    - Substituir `import org.springframework.data.domain.Page` por `import com.github.marcelomachadoxd.carteiraclientes.dto.PageResponse` (manter `Pageable`)
    - Alterar o tipo de retorno de `findAllPageable` de `ResponseEntity<Page<UserDTO>>` para `ResponseEntity<PageResponse<UserDTO>>`
    - Envolver o resultado do service com `new PageResponse<>(users)` antes de passar ao `ResponseEntity.ok().body(...)`
    - Não alterar `findById`, `delete` e `insert` (endpoints não paginados ou de escrita)
    - _Requirements: 2.3, 3.5_

  - [~] 5.5 Escrever testes unitários para `PageResponse<T>`
    - Testar o construtor com um `Page<T>` mockado verificando que todos os 7 campos são corretamente mapeados
    - Testar `PageResponse<T>` com página vazia: `content` vazio, `totalElements=0`, `totalPages=0`, `first=true`, `last=true`
    - Testar `PageResponse<T>` com página intermediária (não primeira, não última): `first=false`, `last=false`
    - Testar que `ClienteResource`, `VisitaResource` e `UserResource` retornam `PageResponse<T>` nos endpoints paginados (via `@WebMvcTest` ou `MockMvc`)
    - Usar `tools.jackson.databind.ObjectMapper` (Jackson 3) para serialização/deserialização nos testes
    - _Requirements: 2.3, 3.3, 3.4, 3.5, 3.8_

  - [~] 5.6 Verificar que o teste de exploração (Property 1 — PageImpl) agora passa
    - **Property 1: Expected Behavior** - Estrutura JSON Estável nos Endpoints Paginados
    - **IMPORTANTE**: Re-executar o MESMO teste da tarefa 3 — NÃO escrever um novo teste
    - Chamar `GET /clientes` e verificar que o JSON contém os 7 campos: `content`, `totalElements`, `totalPages`, `number`, `size`, `first`, `last`
    - Chamar `GET /visitas` e verificar os 7 campos no JSON
    - Chamar `GET /users` e verificar os 7 campos no JSON
    - Verificar que a string `"Serializing PageImpl instances as-is is not supported"` NÃO aparece no log
    - **RESULTADO ESPERADO**: Teste PASSA (confirma que o Bug 3 está corrigido)
    - _Requirements: 2.3_

  - [~] 5.7 Verificar que os testes de preservação ainda passam após a correção do Bug 3
    - **Property 2: Preservation** - Dados e Comportamento Funcional Inalterados
    - **IMPORTANTE**: Re-executar os MESMOS testes da tarefa 2 — NÃO escrever novos testes
    - Verificar que `GET /clientes/id/{id}` continua retornando `200 OK` com `ClienteDTO` correto
    - Verificar que `GET /roles` continua retornando `200 OK` com `List<RoleDTO>` (não `PageResponse`)
    - Verificar que os dados em `content`, `totalElements`, `totalPages`, `number` e `size` são idênticos aos que eram retornados pelo `PageImpl` anterior
    - Verificar que `POST /clientes`, `PUT /clientes/id/{id}` e `DELETE /clientes/id/{id}` continuam com os mesmos status HTTP
    - Executar `mvnw.cmd test` e confirmar `failures=0` e `errors=0`
    - **RESULTADO ESPERADO**: Testes PASSAM (confirma que não há regressões)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

- [~] 6. Checkpoint — Garantir que todos os testes passam
  - Executar `mvnw.cmd test` e confirmar `failures=0` e `errors=0` com o mesmo número de testes que passavam antes da correção
  - Executar `mvnw.cmd verify` e confirmar que a cobertura JaCoCo ≥ 70% continua sendo atingida com o novo `PageResponse<T>` incluído na cobertura
  - Confirmar que nenhum dos três warnings aparece: `"Mockito is currently self-attaching"`, `"terminally deprecated method in java.lang.System"`, `"Serializing PageImpl instances as-is is not supported"`
  - Confirmar que `GET /roles` continua retornando `List<RoleDTO>` (não `PageResponse`) — `RoleResource` não foi alterado
  - Perguntar ao usuário se houver dúvidas ou comportamentos inesperados
