# Documento de Requisitos

## Introduction

O projeto CarteiraClientes é uma API REST para corretores de imóveis gerenciarem sua carteira de clientes. Atualmente, a cobertura de testes está abaixo de 70%, com testes existentes apenas na camada de serviço (unit e integração). As camadas de resource (controllers) e repository estão completamente sem cobertura, e alguns cenários de erro e fluxos alternativos nos serviços também não são testados.

Esta feature define os requisitos para elevar a cobertura de testes para pelo menos 70%, adicionando testes nas camadas de resource, repository e complementando os testes de serviço existentes.

## Glossário

- **Suite_de_Testes**: Conjunto completo de classes de teste do projeto CarteiraClientes
- **Resource_Test**: Teste de integração de controller usando `@WebMvcTest` ou `@SpringBootTest` com `MockMvc`
- **Service_Test**: Teste unitário de serviço usando Mockito com `@ExtendWith(SpringExtension.class)`
- **Repository_Test**: Teste de repositório usando `@DataJpaTest` com banco H2 em memória
- **Cobertura**: Percentual de linhas/branches do código de produção exercitados pelos testes, medido pelo JaCoCo
- **ClienteService**: Serviço responsável pela lógica de negócio de clientes
- **VisitaService**: Serviço responsável pela lógica de negócio de visitas
- **UserService**: Serviço responsável pela lógica de negócio de usuários
- **RoleService**: Serviço responsável pela lógica de negócio de perfis de acesso
- **ClienteResource**: Controller REST para o endpoint `/clientes`
- **VisitaResource**: Controller REST para o endpoint `/visitas`
- **UserResource**: Controller REST para o endpoint `/users`
- **RoleResource**: Controller REST para o endpoint `/roles`
- **ClienteRepository**: Repositório JPA com queries customizadas para busca de clientes
- **VisitaRepository**: Repositório JPA com queries customizadas para busca de visitas
- **ResourceNotFoundException**: Exceção lançada quando um recurso não é encontrado (HTTP 404)
- **DatabaseException**: Exceção lançada em erros de banco de dados (HTTP 400)

---

## Requirements

### Requirement 1: Configuração de Medição de Cobertura

**User Story:** Como desenvolvedor, quero medir a cobertura de testes automaticamente no build, para que eu possa verificar se a meta de 70% foi atingida.

#### Critérios de Aceitação

1. THE Suite_de_Testes SHALL incluir o plugin JaCoCo configurado no `pom.xml` para gerar relatório de cobertura durante a fase `test` do Maven
2. WHEN o comando `mvnw test` é executado, THE Suite_de_Testes SHALL gerar o relatório de cobertura em `target/site/jacoco/index.html`
3. WHEN a cobertura de linhas das classes não excluídas está abaixo de 70%, THE Suite_de_Testes SHALL falhar o build com mensagem de erro indicando a cobertura insuficiente; IF a cobertura está igual ou acima de 70%, THE Suite_de_Testes SHALL concluir o build com sucesso
4. WHERE o JaCoCo está configurado, THE Suite_de_Testes SHALL excluir da medição de cobertura as classes `CarteiraClientesApplication` e `SwaggerConfig`
5. THE Suite_de_Testes SHALL aplicar a regra de cobertura mínima de 70% somente sobre as classes não excluídas pelo critério 4

---

### Requirement 2: Testes de Repositório — ClienteRepository

**User Story:** Como desenvolvedor, quero testes para as queries customizadas do ClienteRepository, para que eu possa garantir que a busca por nome e por interesses retorna os resultados corretos.

#### Critérios de Aceitação

1. WHEN um cliente com nome "João Silva" existe no banco, THE ClienteRepository SHALL retornar esse cliente ao executar `findByNome("joão", pageable)` (busca case-insensitive com prefixo)
2. WHEN nenhum cliente corresponde ao nome pesquisado, THE ClienteRepository SHALL retornar uma página vazia
3. WHEN um cliente tem `qtdQuartos = 3`, `qtdBanheiros = 2`, `qtdVagas = 1`, `metragem = 80.0`, `valorMaximo = 300000.0` e a busca por interesses usa os mesmos valores com `margem = 0`, THE ClienteRepository SHALL incluir esse cliente no resultado; a comparação de `metragem` SHALL verificar que o valor buscado é menor ou igual ao do cliente, e a comparação de `valorMaximo` SHALL verificar que o valor buscado é menor ou igual ao do cliente
4. WHEN a busca por interesses usa `margem = 10` e o `valorMaximo` buscado está dentro de 10% acima do `valorMaximo` do cliente, THE ClienteRepository SHALL incluir esse cliente no resultado; a tolerância de margem SHALL ser aplicada apenas a `valorMaximo` e `metragem`; os campos `qtdQuartos`, `qtdBanheiros` e `qtdVagas` SHALL ser comparados sem margem
5. WHEN todos os parâmetros de interesse (`qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem`, `valorMaximo`) são `0`, THE ClienteRepository SHALL retornar todos os clientes cadastrados, pois o valor `0` é tratado como curinga (sem filtro)
6. WHEN um cliente tem `qtdQuartos` menor que o valor buscado, ou `qtdBanheiros` menor que o valor buscado, ou `qtdVagas` menor que o valor buscado, THE ClienteRepository SHALL excluir esse cliente do resultado da busca por interesses
7. WHEN um cliente é inserido via `save` e o id gerado é usado em `findById`, THE ClienteRepository SHALL retornar um `Optional` presente contendo o mesmo cliente com os mesmos valores de campos persistidos

---

### Requirement 3: Testes de Repositório — VisitaRepository

**User Story:** Como desenvolvedor, quero testes para as queries customizadas do VisitaRepository, para que eu possa garantir que as buscas por responsável, cliente e combinação retornam os dados corretos.

#### Critérios de Aceitação

1. WHEN uma visita com `responsavel.id = 2` existe no banco e `findByResponsavelId(2, PageRequest.of(0, 10))` é executado, THE VisitaRepository SHALL retornar uma página com `totalElements >= 1` onde todos os itens têm `responsavel.id == 2`
2. WHEN uma visita com `cliente.id = 1` existe no banco e `findByClienteId(1, PageRequest.of(0, 10))` é executado, THE VisitaRepository SHALL retornar uma página com `totalElements >= 1` onde todos os itens têm `cliente.id == 1`
3. WHEN uma visita é persistida com `cliente.id = cliId` e `responsavel.id = respId` e `findByClienteAndResponsavelId(cliId, respId, PageRequest.of(0, 10))` é executado, THE VisitaRepository SHALL retornar uma página com `totalElements >= 1` contendo essa visita
4. IF nenhuma visita com `responsavel.id = Long.MAX_VALUE` existe no banco, THEN `findByResponsavelId(Long.MAX_VALUE, PageRequest.of(0, 10))` SHALL retornar uma página com `totalElements == 0`
5. IF nenhuma visita com a combinação `cliente.id = Long.MAX_VALUE` e `responsavel.id = Long.MAX_VALUE` existe no banco, THEN `findByClienteAndResponsavelId(Long.MAX_VALUE, Long.MAX_VALUE, PageRequest.of(0, 10))` SHALL retornar uma página com `totalElements == 0`

---

### Requirement 4: Testes de Serviço — Cobertura de Cenários Faltantes

**User Story:** Como desenvolvedor, quero complementar os testes de serviço existentes com os cenários ainda não cobertos, para que toda a lógica de negócio dos serviços esteja validada.

#### Critérios de Aceitação

1. WHEN `ClienteService.delete` é chamado com um id existente, THE ClienteService SHALL invocar `clienteRepository.deleteById` com esse id exato e concluir sem lançar exceção
2. WHEN `ClienteService.delete` é chamado e `clienteRepository.deleteById` lança qualquer `RuntimeException`, THE ClienteService SHALL capturar essa exceção e lançar `DatabaseException`
3. WHEN `ClienteService.update` é chamado com um id existente e um `ClienteDTO` válido, THE ClienteService SHALL invocar `clienteRepository.save` com uma entidade contendo os novos valores do DTO e concluir sem lançar exceção
4. WHEN `ClienteService.update` é chamado com um id inexistente, THE ClienteService SHALL lançar `NoSuchElementException` (comportamento atual não tratado pelo serviço)
5. WHEN `ClienteService.findByInteresses` é chamado com `qtdQuartos = 2`, `qtdBanheiros = 1`, `qtdVagas = 1`, `metragem = 60.0`, `valorMaximo = 200000.0`, `margem = 0`, e um `Pageable` válido, THE ClienteService SHALL retornar uma `Page<ClienteDTO>` não nula
6. WHEN `UserService.delete` é chamado com um id existente, THE UserService SHALL invocar `userRepository.deleteById` com esse id exato e concluir sem lançar exceção
7. WHEN `UserService.delete` é chamado e `userRepository.deleteById` lança qualquer `RuntimeException`, THE UserService SHALL capturar essa exceção e lançar `DatabaseException`
8. WHEN `UserService.findAllPageable` é chamado com um `Pageable` válido, THE UserService SHALL retornar uma `Page<UserDTO>` não nula com os usuários existentes
9. WHEN `RoleService.findAll` é chamado, THE RoleService SHALL retornar uma `List<RoleDTO>` não nula com todos os perfis existentes
10. WHEN `RoleService.insert` é chamado com um `RoleDTO` com `nome = "ROLE_TEST"`, THE RoleService SHALL invocar `roleRepository.save` e retornar um `RoleDTO` com `nome = "ROLE_TEST"`

---

### Requirement 5: Testes de Resource — ClienteResource

**User Story:** Como desenvolvedor, quero testes de integração para o ClienteResource, para que eu possa garantir que os endpoints HTTP respondem com os status codes e payloads corretos.

#### Critérios de Aceitação

1. WHEN uma requisição `GET /clientes/id/{id}` é feita com um id existente, THE ClienteResource SHALL responder com HTTP 200 e o corpo contendo o `ClienteDTO` correspondente
2. WHEN uma requisição `GET /clientes/id/{id}` é feita com um id inexistente, THE ClienteResource SHALL responder com HTTP 404
3. WHEN uma requisição `GET /clientes/nome/{nome}` é feita com um nome existente, THE ClienteResource SHALL responder com HTTP 200 e uma página com pelo menos um resultado
4. WHEN uma requisição `POST /clientes` é feita com um corpo JSON válido, THE ClienteResource SHALL responder com HTTP 201 e o header `Location` apontando para o novo recurso
5. WHEN uma requisição `POST /clientes` é feita com um campo `email` em formato inválido, THE ClienteResource SHALL responder com HTTP 422 e o corpo contendo um objeto `errors` com pelo menos uma entrada com os campos `fieldName` e `message`
6. WHEN uma requisição `DELETE /clientes/id/{id}` é feita com um id existente, THE ClienteResource SHALL responder com HTTP 204
7. WHEN uma requisição `PUT /clientes/id/{id}` é feita com um corpo JSON válido e id existente, THE ClienteResource SHALL responder com HTTP 200; IF o id não existe, THE ClienteResource SHALL responder com HTTP 404
8. WHEN uma requisição `GET /clientes` é feita com os parâmetros `margem`, `qtdQuartos`, `qtdBanheiros`, `qtdVagas`, `metragem` e `valorMaximo`, THE ClienteResource SHALL responder com HTTP 200 e uma página de clientes filtrados
9. WHEN uma requisição `DELETE /clientes/id/{id}` é feita com um id inexistente, THE ClienteResource SHALL responder com HTTP 404

---

### Requirement 6: Testes de Resource — VisitaResource

**User Story:** Como desenvolvedor, quero testes de integração para o VisitaResource, para que eu possa garantir que os endpoints de visitas respondem corretamente.

#### Critérios de Aceitação

1. WHEN uma requisição `GET /visitas/{id}` é feita com um id existente, THE VisitaResource SHALL responder com HTTP 200 e o `VisitaDTO` correspondente no corpo
2. WHEN uma requisição `GET /visitas/{id}` é feita com um id inexistente, THE VisitaResource SHALL responder com HTTP 404
3. WHEN uma requisição `GET /visitas/responsavel/{id}` é feita com um id de responsável existente, THE VisitaResource SHALL responder com HTTP 200 e uma página paginada de `VisitaDTO`
4. WHEN uma requisição `GET /visitas/cliente/{id}` é feita com um id de cliente existente, THE VisitaResource SHALL responder com HTTP 200 e uma página paginada de `VisitaDTO`
5. WHEN uma requisição `POST /visitas` é feita com um corpo JSON contendo `responsavel.id`, `cliente.id` e `dataVisita` válidos, THE VisitaResource SHALL responder com HTTP 200 e o `VisitaDTO` criado no corpo
6. WHEN uma requisição `DELETE /visitas/{id}` é feita com um id existente, THE VisitaResource SHALL responder com HTTP 204; IF o id não existe, THE VisitaResource SHALL responder com HTTP 404
7. WHEN uma requisição `POST /visitas` é feita com um corpo JSON sem o campo obrigatório `dataVisita`, THE VisitaResource SHALL responder com HTTP 422
8. WHEN uma requisição `POST /visitas` é feita referenciando um `responsavel.id` ou `cliente.id` inexistente, THE VisitaResource SHALL responder com HTTP 404

---

### Requirement 7: Testes de Resource — UserResource e RoleResource

**User Story:** Como desenvolvedor, quero testes de integração para UserResource e RoleResource, para que eu possa garantir que os endpoints de usuários e perfis respondem corretamente.

#### Critérios de Aceitação

1. WHEN uma requisição `GET /users` é feita, THE UserResource SHALL responder com HTTP 200 e uma página de `UserDTO`
2. WHEN uma requisição `GET /users/{id}` é feita com um id existente, THE UserResource SHALL responder com HTTP 200 e o `UserDTO` correspondente
3. WHEN uma requisição `GET /users/{id}` é feita com um id inexistente, THE UserResource SHALL responder com HTTP 404
4. WHEN uma requisição `POST /users` é feita com um corpo JSON contendo `nome`, `email`, `password` e `acessoId` válidos, THE UserResource SHALL responder com HTTP 200 e o `UserDTO` criado
5. WHEN uma requisição `DELETE /users/{id}` é feita com um id existente, THE UserResource SHALL responder com HTTP 204
6. WHEN uma requisição `GET /roles` é feita, THE RoleResource SHALL responder com HTTP 200 e a lista de `RoleDTO`
7. WHEN uma requisição `POST /roles` é feita com um corpo JSON contendo o campo `nome`, THE RoleResource SHALL responder com HTTP 201 e o `RoleDTO` criado
8. WHEN uma requisição `POST /users` é feita com um corpo JSON faltando campos obrigatórios, THE UserResource SHALL responder com HTTP 422
9. WHEN uma requisição `DELETE /users/{id}` é feita com um id inexistente, THE UserResource SHALL responder com HTTP 400

---

### Requirement 8: Testes do Tratamento de Exceções

**User Story:** Como desenvolvedor, quero testes para o `ResourceExceptionHandler`, para que eu possa garantir que erros são mapeados para respostas HTTP estruturadas.

#### Critérios de Aceitação

1. WHEN um endpoint lança `ResourceNotFoundException`, THE ResourceExceptionHandler SHALL retornar HTTP 404 com um corpo JSON contendo os campos `timestamp`, `status`, `error`, `message` e `path`
2. WHEN um endpoint lança `DatabaseException`, THE ResourceExceptionHandler SHALL retornar HTTP 400 com um corpo JSON contendo os campos `timestamp`, `status`, `error`, `message` e `path`
3. WHEN uma requisição com corpo inválido é recebida e a validação de bean falha, THE ResourceExceptionHandler SHALL retornar HTTP 422 com um corpo JSON do tipo `ValidationError` contendo a lista `errors` onde cada entrada tem os campos `fieldName` e `fieldMessage`
4. WHEN um endpoint lança uma `RuntimeException` não mapeada pelos handlers específicos de `ResourceNotFoundException` e `DatabaseException`, THE ResourceExceptionHandler SHALL retornar HTTP 500 com um corpo JSON contendo os campos `timestamp`, `status`, `error` e `message`
