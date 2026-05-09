# Plano de Implementação: swagger-enrichment

## Visão Geral

Enriquecer a documentação Swagger da API CarteiraClientes adotando duas camadas: interfaces contratuais (`*ResourceDocumentation`) que concentram todas as anotações OpenAPI, e um `SwaggerConfig` enriquecido com segurança Bearer JWT e respostas de erro globais. Os controllers ficam livres de qualquer anotação Swagger.

> **Fora de escopo:** testes ArchUnit de verificação estrutural foram movidos para `.kiro/jira-tasks/MyFriend-002-archUnit.md`.

## Tasks

- [x] 1. Enriquecer o `SwaggerConfig`
  - Enriquecer o bean `OpenAPI` no `SwaggerConfig` com:
    - `SecurityScheme` do tipo `http`, `scheme = "bearer"`, `bearerFormat = "JWT"`, nome `"bearerAuth"`
    - `SecurityRequirement` global com `addList("bearerAuth")`
    - Respostas globais para `400`, `401` e `500` nos `components`, cada uma com schema referenciando `StandardError`
    - Descrição contendo as expressões `"corretores de imóveis"` e `"carteira de clientes"`
  - Garantir que todos os imports sejam de `io.swagger.v3.oas.*`, `org.springframework.*` ou `jakarta.*` — nenhum `javax.*`
  - _Requisitos: 2.1, 2.2, 2.3, 2.4_

- [x] 2. Criar `ClienteResourceDocumentation` e enriquecer `ClienteDTO`
  - Criar o pacote `resources/documentation/`
  - Criar a interface `ClienteResourceDocumentation` com `@Tag` e os 6 métodos anotados com `@Operation`, `@Parameter`, `@ApiResponse` e `@RequestBody` conforme o design
  - Adicionar `@Schema(example = ...)` nos 9 campos do `ClienteDTO`
  - Fazer `ClienteResource` implementar `ClienteResourceDocumentation`
  - _Requisitos: 1.1, 1.9, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 2.1 Criar a interface `ClienteResourceDocumentation`
    - Declarar os 6 métodos com `@Operation` (summary ≤ 120 chars, description com ≥ 1 frase), `@Parameter` para os 6 query params de `GET /clientes`, `@ApiResponse` para 200/201/204/404/422 conforme a tabela do design, e `@RequestBody` com exemplo de payload para `POST /clientes`
    - _Requisitos: 1.1, 3.1, 3.2, 3.4, 3.5, 3.6_

  - [x] 2.2 Adicionar `@Schema(example = ...)` nos campos do `ClienteDTO`
    - Anotar os 9 campos com os valores de exemplo definidos no design: `id=1`, `nome="Maria Silva"`, `email="maria.silva@email.com"`, `qtdQuartos=2`, `qtdBanheiros=1`, `qtdVagas=1`, `metragem=65`, `valorMaximo=350000`, `obs="Prefere apartamento em andar alto, aceita condomínio até R$ 800"`
    - _Requisitos: 3.3_

  - [x] 2.3 Fazer `ClienteResource` implementar `ClienteResourceDocumentation`
    - Adicionar `implements ClienteResourceDocumentation` na declaração da classe
    - Verificar que nenhuma anotação Swagger foi adicionada diretamente nos métodos do controller
    - _Requisitos: 1.1, 1.9_

- [x] 3. Criar `VisitaResourceDocumentation` e enriquecer `VisitaDTO`
  - Criar a interface `VisitaResourceDocumentation` com `@Tag` e os 6 métodos anotados conforme o design
  - Adicionar `@Schema(example = ...)` nos campos primitivos do `VisitaDTO` e nos objetos aninhados
  - Fazer `VisitaResource` implementar `VisitaResourceDocumentation`
  - _Requisitos: 1.2, 1.10, 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 3.1 Criar a interface `VisitaResourceDocumentation`
    - Declarar os 6 métodos com `@Operation` (summary ≤ 10 palavras, description com ≥ 1 frase de domínio), `@Parameter` para `cliId` e `respId` com `required=false` e `example=1`, `@ApiResponse` para 200/204/404/422 conforme a tabela do design, e `@RequestBody` com exemplo de payload para `POST /visitas`
    - Atenção: `VisitaResource` tem dois métodos com o nome `findByClienteId` — a interface deve declarar ambos com as assinaturas exatas para resolução correta pelo Java
    - _Requisitos: 1.2, 4.1, 4.2, 4.4, 4.5_

  - [x] 3.2 Adicionar `@Schema(example = ...)` nos campos do `VisitaDTO`
    - Anotar os campos primitivos (`id=1`, `dataVisita="2025-03-15T14:30:00Z"`, `obs="Cliente gostou do imóvel, aguardando proposta"`, `satisfacao=true`) e os campos dos objetos aninhados (`responsavel.id=2`, `responsavel.nome="João Corretor"`, `cliente.id=1`, `cliente.nome="Maria Silva"`)
    - _Requisitos: 4.3_

  - [x] 3.3 Fazer `VisitaResource` implementar `VisitaResourceDocumentation`
    - Adicionar `implements VisitaResourceDocumentation` na declaração da classe
    - Verificar que nenhuma anotação Swagger foi adicionada diretamente nos métodos do controller
    - _Requisitos: 1.2, 1.10_

- [ ] 4. Criar `UserResourceDocumentation` e enriquecer `UserDTO` / `UserInsertDTO`
  - Criar a interface `UserResourceDocumentation` com `@Tag` e os 4 métodos anotados conforme o design
  - Adicionar `@Schema(example = ...)` nos campos de `UserDTO` e `UserInsertDTO`
  - Fazer `UserResource` implementar `UserResourceDocumentation`
  - _Requisitos: 1.3, 1.11, 5.1, 5.2, 5.3, 5.4_

  - [x] 4.1 Criar a interface `UserResourceDocumentation`
    - Declarar os 4 métodos com `@Operation` (summary não vazio, description com ≥ 1 frase), `@ApiResponse` para 200/204/404/422 conforme a tabela do design, e `@RequestBody` com exemplo de payload de `UserInsertDTO` para `POST /users`
    - _Requisitos: 1.3, 5.1, 5.2, 5.3, 5.4_

  - [~] 4.2 Adicionar `@Schema(example = ...)` nos campos de `UserDTO` e `UserInsertDTO`
    - `UserDTO`: `id=2`, `nome="João Corretor"`, `email="joao.corretor@imobiliaria.com.br"`
    - `UserInsertDTO`: `nome="João Corretor"`, `email="joao.corretor@imobiliaria.com.br"`, `acessoId=1`, `password="senha123"`
    - _Requisitos: 5.1_

  - [~] 4.3 Fazer `UserResource` implementar `UserResourceDocumentation`
    - Adicionar `implements UserResourceDocumentation` na declaração da classe
    - Verificar que nenhuma anotação Swagger foi adicionada diretamente nos métodos do controller
    - _Requisitos: 1.3, 1.11_

- [ ] 5. Criar `RoleResourceDocumentation` e enriquecer `RoleDTO`
  - Criar a interface `RoleResourceDocumentation` com `@Tag` e os 2 métodos anotados conforme o design
  - Adicionar `@Schema(example = ...)` nos campos de `RoleDTO`
  - Fazer `RoleResource` implementar `RoleResourceDocumentation`
  - _Requisitos: 1.4, 1.12, 6.1, 6.2, 6.3, 6.4_

  - [~] 5.1 Criar a interface `RoleResourceDocumentation`
    - Declarar os 2 métodos com `@Operation` (summary ≤ 10 palavras, description com ≥ 1 frase), `@ApiResponse` para 200/201 com `content` referenciando `RoleDTO`, e `@RequestBody` com exemplo de payload `nome="ROLE_CORRETOR"` (sem `id`) para `POST /roles`
    - _Requisitos: 1.4, 6.1, 6.3, 6.4_

  - [~] 5.2 Adicionar `@Schema(example = ...)` nos campos de `RoleDTO`
    - Anotar `id=1` e `nome="ROLE_CORRETOR"`
    - _Requisitos: 6.2_

  - [~] 5.3 Fazer `RoleResource` implementar `RoleResourceDocumentation`
    - Adicionar `implements RoleResourceDocumentation` na declaração da classe
    - Verificar que nenhuma anotação Swagger foi adicionada diretamente nos métodos do controller
    - _Requisitos: 1.4, 1.12_

- [ ] 6. Criar testes de smoke do bean `OpenAPI`
  - Criar classe de teste que carrega o contexto Spring e inspeciona o bean `OpenAPI`
  - _Requisitos: 2.1, 2.2, 2.3, 2.4_

  - [~] 6.1 Criar `SwaggerConfigTest` com testes de smoke do bean `OpenAPI`
    - Usar `@SpringBootTest` + `@ActiveProfiles("test")`
    - Verificar título `"CarteiraClientes API"`, versão `"1.0.0"` e descrição contendo `"corretores de imóveis"` e `"carteira de clientes"` (Requisito 2.1)
    - Verificar `SecurityScheme` `"bearerAuth"` do tipo `HTTP`/`bearer`/`JWT` (Requisito 2.2)
    - Verificar `SecurityRequirement` global com `"bearerAuth"` (Requisito 2.3)
    - Verificar respostas globais `400`, `401` e `500` nos `components` (Requisito 2.4)
    - _Requisitos: 2.1, 2.2, 2.3, 2.4_

- [ ] 7. Criar testes de integração MockMvc para verificar roteamento preservado
  - Criar classe de teste com `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`
  - Cobrir todos os 18 endpoints dos 4 resources
  - _Requisitos: 1.9, 1.10, 1.11, 1.12, 7.1, 7.3_

  - [~] 7.1 Criar `ResourceRoutingIntegrationTest` para `ClienteResource` e `RoleResource`
    - Testar os 6 endpoints de `/clientes` (GET /clientes/id/1, GET /clientes/nome/Cliente, GET /clientes, POST /clientes, PUT /clientes/id/1, DELETE /clientes/id/1) verificando que os status HTTP são os mesmos de antes da refatoração
    - Testar os 2 endpoints de `/roles` (GET /roles, POST /roles) verificando status 200 e 201
    - Usar os IDs do seed data: `Cliente id=1`, `Role id=1`
    - _Requisitos: 1.9, 1.12, 7.1, 7.3_

  - [~] 7.2 Criar testes MockMvc para `VisitaResource` e `UserResource`
    - Testar os 6 endpoints de `/visitas` (GET /visitas/1, GET /visitas/responsavel/2, GET /visitas/cliente/1, GET /visitas, POST /visitas, DELETE /visitas/1) verificando status HTTP preservados
    - Testar os 4 endpoints de `/users` (GET /users, GET /users/2, POST /users, DELETE /users/2) verificando status HTTP preservados
    - Usar os IDs do seed data: `Visita id=1`, `User id=2`
    - _Requisitos: 1.10, 1.11, 7.1, 7.3_

- [~] 8. Checkpoint final — Verificar build completo e cobertura JaCoCo
  - Garantir que `mvnw.cmd verify` retorna `BUILD SUCCESS` e cobertura JaCoCo ≥ 70%, ask the user if questions arise.

## Notas

- Cada task referencia requisitos específicos para rastreabilidade
- As interfaces contratuais **não devem** declarar anotações de mapeamento HTTP (`@GetMapping`, `@PostMapping`, etc.) — apenas anotações OpenAPI — para evitar `Ambiguous mapping` no Spring
- O `VisitaResource` tem dois métodos com o nome `findByClienteId`; a interface `VisitaResourceDocumentation` deve declarar ambos com assinaturas exatas
- Todos os imports devem usar `jakarta.*` — nunca `javax.*` (Spring Boot 4 + Java 25)
- O `SwaggerConfig` está excluído da métrica de cobertura JaCoCo (configurado no `pom.xml`)
- Interfaces Java puras não geram bytecode de método e não afetam a cobertura JaCoCo
- Testes ArchUnit (Propriedade 1 + verificação de `javax.*`) estão em `.kiro/jira-tasks/MyFriend-002-archUnit.md`

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1", "2.2", "3.1", "3.2", "4.1", "4.2", "5.1", "5.2"] },
    { "id": 2, "tasks": ["2.3", "3.3", "4.3", "5.3"] },
    { "id": 3, "tasks": ["6.1", "7.1", "7.2"] }
  ]
}
```
