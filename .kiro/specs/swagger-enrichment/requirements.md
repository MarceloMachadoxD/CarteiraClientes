# Requirements Document

## Introduction

A API CarteiraClientes possui documentação Swagger básica — apenas metadados globais mínimos no `SwaggerConfig` e nenhuma anotação nos controllers. O objetivo desta feature é transformar essa documentação em um guia de uso integrado e prático, sem poluir o código de negócio.

A abordagem adota duas camadas:

1. **Interfaces Contratuais** — para cada `*Resource`, criar uma interface correspondente (ex: `ClienteResourceDocumentation`) que concentra todas as anotações OpenAPI (`@Operation`, `@Parameter`, `@ApiResponse`, exemplos de payload). Os controllers passam a implementar essas interfaces e ficam livres de qualquer anotação Swagger.
2. **Configuração Centralizada** — o bean `SwaggerConfig` é enriquecido com metadados globais completos, esquema de segurança (Bearer JWT) e respostas de erro padrão (400, 401, 500) aplicadas globalmente, eliminando repetição nas interfaces.

O resultado esperado é uma Swagger UI que funcione como guia de uso autossuficiente para consumidores da API, com exemplos de negócio coerentes (ex: "Apartamento 2 quartos", "Cliente VIP").

## Glossary

- **SwaggerConfig**: classe `com.github.marcelomachadoxd.carteiraclientes.config.SwaggerConfig` que produz o bean `OpenAPI`.
- **ClienteResourceDocumentation**: interface contratual de documentação para `ClienteResource`.
- **VisitaResourceDocumentation**: interface contratual de documentação para `VisitaResource`.
- **UserResourceDocumentation**: interface contratual de documentação para `UserResource`.
- **RoleResourceDocumentation**: interface contratual de documentação para `RoleResource`.
- **Controller**: qualquer classe `*Resource` anotada com `@RestController`.
- **Interface_Contratual**: interface Java que concentra exclusivamente anotações OpenAPI/Swagger para um Controller.
- **OpenAPI_Bean**: bean do tipo `OpenAPI` produzido pelo `SwaggerConfig`.
- **Endpoint**: método HTTP mapeado em um Controller.
- **Payload_Exemplo**: valor de exemplo mockado com dados coerentes ao domínio imobiliário (ex: nomes reais, valores plausíveis de imóveis).
- **StandardError**: DTO `com.github.marcelomachadoxd.carteiraclientes.resources.exceptions.StandardError` com campos `timestamp`, `status`, `error`, `message`, `path`.
- **ValidationError**: extensão de `StandardError` com lista de `FieldMessage` por campo inválido.
- **ClienteDTO**: DTO com campos `id`, `nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`, `obs`.
- **VisitaDTO**: DTO com campos `id`, `dataVisita`, `obs`, `satisfacao`, `responsavel` (UserDTO), `cliente` (ClienteDadosBasicosDTO).
- **UserDTO**: DTO com campos `id`, `nome`, `email`, `acesso` (Set\<RoleDTO\>).
- **UserInsertDTO**: DTO de criação de usuário com campos `nome`, `email`, `acessoId`, `password`.
- **RoleDTO**: DTO com campos `id`, `nome`.

---

## Requirements

### Requirement 1: Separação de Responsabilidades — Interfaces Contratuais

**User Story:** Como desenvolvedor que mantém a API, quero que as anotações Swagger residam exclusivamente em interfaces separadas, para que os controllers permaneçam limpos e focados apenas na lógica de roteamento.

#### Acceptance Criteria

1. THE **ClienteResourceDocumentation** SHALL declarar os 6 métodos públicos de `ClienteResource` (`findById`, `findByNome`, `findByInteresses`, `insert`, `update`, `delete`), cada um anotado com `@Operation`, `@Parameter` (onde aplicável) e `@ApiResponse`.
2. THE **VisitaResourceDocumentation** SHALL declarar os 6 métodos públicos de `VisitaResource` (`findById`, `findByResponsavelId`, `findByClienteId`, `findByClienteAndResponsavelId`, `insert`, `delete`), cada um anotado com `@Operation`, `@Parameter` (onde aplicável) e `@ApiResponse`.
3. THE **UserResourceDocumentation** SHALL declarar os 4 métodos públicos de `UserResource` (`findAllPageable`, `findById`, `insert`, `delete`), cada um anotado com `@Operation`, `@Parameter` (onde aplicável) e `@ApiResponse`.
4. THE **RoleResourceDocumentation** SHALL declarar os 2 métodos públicos de `RoleResource` (`findAll`, `insert`), cada um anotado com `@Operation` e `@ApiResponse`.
5. WHEN o `ClienteResource` implementa `ClienteResourceDocumentation` e uma requisição HTTP é enviada para cada um dos 6 endpoints de `/clientes`, THE **Spring_Framework** SHALL responder com o mesmo método HTTP, path e status HTTP que respondia antes da implementação da interface.
6. WHEN o `VisitaResource` implementa `VisitaResourceDocumentation` e uma requisição HTTP é enviada para cada um dos 6 endpoints de `/visitas`, THE **Spring_Framework** SHALL responder com o mesmo método HTTP, path e status HTTP que respondia antes da implementação da interface.
7. WHEN o `UserResource` implementa `UserResourceDocumentation` e uma requisição HTTP é enviada para cada um dos 4 endpoints de `/users`, THE **Spring_Framework** SHALL responder com o mesmo método HTTP, path e status HTTP que respondia antes da implementação da interface.
8. WHEN o `RoleResource` implementa `RoleResourceDocumentation` e uma requisição HTTP é enviada para cada um dos 2 endpoints de `/roles`, THE **Spring_Framework** SHALL responder com o mesmo método HTTP, path e status HTTP que respondia antes da implementação da interface.

---

### Requirement 2: Configuração Centralizada do OpenAPI Bean

**User Story:** Como desenvolvedor que consome a API, quero que os metadados globais, o esquema de segurança e as respostas de erro padrão estejam definidos em um único lugar, para que eu não precise repetir essas informações em cada endpoint.

#### Acceptance Criteria

1. THE **SwaggerConfig** SHALL definir no `OpenAPI_Bean` o título `"CarteiraClientes API"`, a versão `"1.0.0"` e uma descrição não vazia que contenha ao menos as expressões "corretores de imóveis" e "carteira de clientes".
2. THE **SwaggerConfig** SHALL definir no `OpenAPI_Bean` um esquema de segurança do tipo `http` com `scheme = "bearer"` e `bearerFormat = "JWT"`, identificado pelo nome `"bearerAuth"`, via `components().addSecuritySchemes("bearerAuth", ...)`.
3. THE **SwaggerConfig** SHALL aplicar o requisito de segurança `"bearerAuth"` globalmente a todos os endpoints via `addSecurityItem(new SecurityRequirement().addList("bearerAuth"))` no `OpenAPI_Bean`.
4. THE **SwaggerConfig** SHALL definir no `OpenAPI_Bean` respostas de erro globais para os códigos HTTP `400`, `401` e `500` via `components().addResponses(...)`, cada uma com descrição em português e schema referenciando `StandardError` via `$ref` ao componente registrado em `components/schemas`.
5. WHEN um endpoint não declara `@ApiResponse` explícito para os códigos `400`, `401` ou `500`, THE **Swagger_UI** SHALL exibir as respostas globais definidas no `OpenAPI_Bean` para esses códigos na documentação desse endpoint.
6. IF um endpoint declara `@ApiResponse` explícito para um dos códigos `400`, `401` ou `500`, THEN THE **Swagger_UI** SHALL exibir a resposta explícita do endpoint para esse código, sem duplicar a resposta global, prevalecendo a definição local.

> **Nota:** A verificação automatizada de imports via ArchUnit (`javax.*` proibido no `SwaggerConfig`) foi movida para `.kiro/jira-tasks/MyFriend-002-archUnit.md`.

---

### Requirement 3: Documentação Detalhada dos Endpoints de Clientes

**User Story:** Como corretor de imóveis que consome a API, quero que cada endpoint de `/clientes` tenha summary e description detalhados com exemplos de negócio, para que eu entenda como usar a API sem precisar de documentação externa.

#### Acceptance Criteria

1. THE **ClienteResourceDocumentation** SHALL declarar `@Operation` em cada um dos 6 endpoints de `ClienteResource` (`GET /clientes/id/{id}`, `GET /clientes/nome/{nome}`, `GET /clientes`, `POST /clientes`, `PUT /clientes/id/{id}`, `DELETE /clientes/id/{id}`), onde cada `@Operation` possui `summary` não vazio com no máximo 120 caracteres e `description` com no mínimo uma frase completa descrevendo o comportamento do endpoint.
2. WHEN o endpoint `GET /clientes` é documentado, THE **ClienteResourceDocumentation** SHALL declarar `@Parameter` para cada um dos 6 query params (`margem`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`), onde a descrição de `margem` explica que é um percentual aplicado sobre `valorMaximo` e `metragem` e que o valor `0` desativa o filtro para aquele parâmetro.
3. THE **ClienteResourceDocumentation** SHALL declarar `@Schema(example = ...)` em cada um dos 9 campos do `ClienteDTO` (`id`, `nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`, `obs`) com os seguintes valores de exemplo: `id = 1`, `nome = "Maria Silva"`, `email = "maria.silva@email.com"`, `qtdQuartos = 2`, `qtdBanheiros = 1`, `qtdVagas = 1`, `metragem = 65`, `valorMaximo = 350000`, `obs = "Prefere apartamento em andar alto, aceita condomínio até R$ 800"`.
4. WHEN o endpoint `POST /clientes` é documentado, THE **ClienteResourceDocumentation** SHALL declarar `@RequestBody` com exemplo de payload contendo todos os campos editáveis do `ClienteDTO`: `nome`, `email`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo` e `obs`, com valores coerentes ao domínio imobiliário.
5. WHEN o endpoint `GET /clientes/id/{id}` ou `DELETE /clientes/id/{id}` recebe um `id` inexistente, THE **ClienteResourceDocumentation** SHALL declarar `@ApiResponse(responseCode = "404", description = "...", content = @Content(schema = @Schema(implementation = StandardError.class)))` para esses endpoints.
6. WHEN o endpoint `POST /clientes` ou `PUT /clientes/id/{id}` recebe um payload com campos inválidos, THE **ClienteResourceDocumentation** SHALL declarar `@ApiResponse(responseCode = "422", description = "...", content = @Content(schema = @Schema(implementation = ValidationError.class)))` para esses endpoints.

---

### Requirement 4: Documentação Detalhada dos Endpoints de Visitas

**User Story:** Como corretor de imóveis que consome a API, quero que cada endpoint de `/visitas` tenha summary, description e exemplos de negócio, para que eu entenda como registrar e consultar visitas a imóveis.

#### Acceptance Criteria

1. THE **VisitaResourceDocumentation** SHALL declarar `@Operation` em cada um dos 6 endpoints de `VisitaResource` (`GET /visitas/{id}`, `GET /visitas/responsavel/{id}`, `GET /visitas/cliente/{id}`, `GET /visitas`, `POST /visitas`, `DELETE /visitas/{id}`), onde cada `@Operation` possui `summary` não vazio com no máximo 10 palavras e `description` com no mínimo uma frase de domínio descrevendo o comportamento do endpoint.
2. WHEN o endpoint `GET /visitas` é documentado, THE **VisitaResourceDocumentation** SHALL declarar `@Parameter` para os query params `cliId` e `respId`, onde cada `@Parameter` especifica `required = false`, `example = 1` (refletindo o `defaultValue = "1"` do controller) e descrição explicando que o filtro é aplicado simultaneamente por cliente e responsável.
3. THE **VisitaResourceDocumentation** SHALL declarar `@Schema(example = ...)` nos campos primitivos do `VisitaDTO` (`id`, `dataVisita`, `obs`, `satisfacao`) e nos campos dos objetos aninhados (`responsavel.id`, `responsavel.nome`, `cliente.id`, `cliente.nome`), com os seguintes valores de exemplo: `id = 1`, `dataVisita = "2025-03-15T14:30:00Z"`, `obs = "Cliente gostou do imóvel, aguardando proposta"`, `satisfacao = true`, `responsavel.id = 2`, `responsavel.nome = "João Corretor"`, `cliente.id = 1`, `cliente.nome = "Maria Silva"`.
4. WHEN o endpoint `POST /visitas` é documentado, THE **VisitaResourceDocumentation** SHALL declarar `@RequestBody` com exemplo de payload contendo os campos primitivos (`dataVisita`, `obs`, `satisfacao`) e os objetos aninhados `responsavel` e `cliente`, cada um com seu `id` preenchido com valor numérico válido.
5. WHEN o endpoint `GET /visitas/{id}` ou `DELETE /visitas/{id}` recebe um `id` inexistente, THE **VisitaResourceDocumentation** SHALL declarar `@ApiResponse(responseCode = "404", description = "...", content = @Content(schema = @Schema(implementation = StandardError.class)))` para esses endpoints.

---

### Requirement 5: Documentação Detalhada dos Endpoints de Usuários

**User Story:** Como administrador do sistema, quero que cada endpoint de `/users` tenha summary, description e exemplos de negócio, para que eu entenda como gerenciar os corretores cadastrados.

#### Acceptance Criteria

1. THE **UserResourceDocumentation** SHALL declarar `@Operation` em cada um dos 4 endpoints de `UserResource` (`GET /users`, `GET /users/{id}`, `POST /users`, `DELETE /users/{id}`), onde cada `@Operation` possui `summary` não vazio e `description` com no mínimo uma frase completa, e SHALL declarar `@Schema(example = ...)` nos campos de `UserDTO` (`id = 2`, `nome = "João Corretor"`, `email = "joao.corretor@imobiliaria.com.br"`) e `UserInsertDTO` (`nome = "João Corretor"`, `email = "joao.corretor@imobiliaria.com.br"`, `acessoId = 1`, `password = "senha123"`).
2. WHEN o endpoint `POST /users` é documentado, THE **UserResourceDocumentation** SHALL declarar `@RequestBody` com exemplo de payload de `UserInsertDTO` contendo os 4 campos obrigatórios: `nome = "João Corretor"`, `email = "joao.corretor@imobiliaria.com.br"`, `acessoId = 1` e `password = "senha123"`.
3. WHEN o endpoint `GET /users/{id}` ou `DELETE /users/{id}` recebe um `id` inexistente, THE **UserResourceDocumentation** SHALL declarar `@ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = StandardError.class)))` para esses endpoints.
4. WHEN o endpoint `POST /users` recebe um payload com campos obrigatórios ausentes ou inválidos, THE **UserResourceDocumentation** SHALL declarar `@ApiResponse(responseCode = "422", description = "Erro de validação nos campos do usuário", content = @Content(schema = @Schema(implementation = ValidationError.class)))`.

---

### Requirement 6: Documentação Detalhada dos Endpoints de Roles

**User Story:** Como administrador do sistema, quero que cada endpoint de `/roles` tenha summary, description e exemplos de negócio, para que eu entenda como gerenciar os perfis de acesso.

#### Acceptance Criteria

1. THE **RoleResourceDocumentation** SHALL declarar `@Operation` em cada um dos 2 endpoints de `RoleResource` (`GET /roles` e `POST /roles`), onde cada `@Operation` possui `summary` não vazio com no máximo 10 palavras e `description` com no mínimo uma frase completa descrevendo o comportamento do endpoint.
2. THE **RoleResourceDocumentation** SHALL declarar `@Schema(example = ...)` nos campos do `RoleDTO` com os valores fixos obrigatórios: `id = 1` e `nome = "ROLE_CORRETOR"`.
3. WHEN o endpoint `POST /roles` é documentado, THE **RoleResourceDocumentation** SHALL declarar `@RequestBody` com exemplo de payload de `RoleDTO` contendo apenas o campo `nome = "ROLE_CORRETOR"`, sem o campo `id` (que é gerado pelo servidor).
4. THE **RoleResourceDocumentation** SHALL declarar `@ApiResponse(responseCode = "200")` para `GET /roles` e `@ApiResponse(responseCode = "201")` para `POST /roles`, cada um com `description` não vazia e `content` referenciando o schema do DTO de resposta correspondente (`RoleDTO`).

---

### Requirement 7: Integridade do Build e Testes Após Refatoração

**User Story:** Como desenvolvedor que mantém a API, quero que o build e todos os testes continuem passando após a extração das anotações para interfaces, para que a refatoração não introduza regressões.

#### Acceptance Criteria

1. WHEN o comando `mvnw.cmd verify` é executado após a implementação completa das interfaces contratuais, THE **Build_System** SHALL retornar `BUILD SUCCESS`.
2. WHEN o comando `mvnw.cmd verify` é executado, THE **JaCoCo** SHALL reportar cobertura de linhas igual ou superior a 70% nas classes não excluídas, sendo que as classes `CarteiraClientesApplication` e `SwaggerConfig` estão explicitamente excluídas da métrica de cobertura.
3. WHEN os testes de integração são executados com `@ActiveProfiles("test")`, THE **Spring_Framework** SHALL inicializar o contexto de aplicação sem lançar `ApplicationContextException` ou `BeanCreationException` para nenhum dos 4 beans de resource (`ClienteResource`, `VisitaResource`, `UserResource`, `RoleResource`).
4. IF o `ClienteResource` implementa `ClienteResourceDocumentation` e o Spring detecta ambiguidade de mapeamento de rotas que impede a inicialização da aplicação, THEN THE **Build_System** SHALL falhar com mensagem de erro que identifica o path HTTP conflitante (ex: `Ambiguous mapping. Cannot map 'clienteResource' method`), permitindo correção antes do deploy.
