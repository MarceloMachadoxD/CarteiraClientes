---
inclusion: always
---

# Estrutura do Projeto

## Package Root

`com.github.marcelomachadoxd.carteiraclientes`

## Organização de Camadas

```
src/main/java/.../carteiraclientes/
├── CarteiraClientesApplication.java   # Entry point Spring Boot
├── config/                            # Beans de configuração (Swagger, etc.)
├── entities/                          # Entidades JPA mapeadas para tabelas do DB
├── dto/                               # Data Transfer Objects para input/output da API
├── repositories/                      # Interfaces Spring Data JPA
├── services/                          # Camada de lógica de negócio
│   └── exceptions/                    # Exceções de serviço: ResourceNotFoundException, DatabaseException
└── resources/                         # Controllers REST (@RestController)
    └── exceptions/                    # Handler global de exceções + modelos de resposta de erro

src/main/resources/
├── application.properties             # Config base (seleção de perfil, placeholders JWT/OAuth2)
├── application-test.properties        # Config H2 in-memory para perfil test/dev
├── data.sql                           # Seed data carregado na inicialização (perfil test)
├── banner.txt                         # Banner customizado do Spring Boot
└── postman-requests/                  # Coleções Postman para testes manuais da API

src/test/                              # Testes JUnit + jqwik
```

## Convenções de Nomenclatura

| Tipo | Padrão | Exemplos |
|---|---|---|
| Entidades | Nome simples | `Cliente`, `Visita`, `User`, `Role` |
| DTOs | `<Entidade>DTO` | `ClienteDTO`, `VisitaDTO` |
| DTOs especializados | `<Entidade><Sufixo>DTO` | `ClienteDadosBasicosDTO`, `UserInsertDTO` |
| Repositories | `<Entidade>Repository` extends `JpaRepository<Entidade, Long>` | `ClienteRepository` |
| Services | `<Entidade>Service` com `@Service` | `ClienteService` |
| Resources (controllers) | `<Entidade>Resource` com `@RestController` | `ClienteResource` |
| Tabelas DB | prefixo `tb_` | `tb_cliente`, `tb_visitas`, `tb_user`, `tb_role` |

## Padrões de Arquitetura

### Camadas e Dependências
- Fluxo estrito: `Resource → Service → Repository`. Resources **nunca** acessam repositories diretamente.
- DTOs são usados na fronteira da API; entidades **nunca** são expostas diretamente nas respostas.
- Todo DTO deve ter um construtor de cópia que aceita a entidade: `new ClienteDTO(cliente)`.

### Tratamento de Erros
- Services lançam `ResourceNotFoundException` (→ HTTP 404) ou `DatabaseException` (→ HTTP 400).
- `ResourceExceptionHandler` (`@ControllerAdvice`) mapeia essas exceções para respostas JSON estruturadas (`StandardError`, `ValidationError`).
- Bean validation (`@Valid`) é aplicado nos request bodies nos resources; erros de validação são capturados pelo handler e retornados como `ValidationError` com lista de `FieldMessage` por campo.

### Queries e Paginação
- Repositories usam JPQL com `@Query` para queries customizadas (ex: busca por prefixo de nome, filtragem por interesses com margem de tolerância).
- Todos os endpoints de listagem retornam `Page<DTO>` usando `Pageable` do Spring Data.

### Convenções de Código
- `equals`/`hashCode` em entidades são baseados **somente** no campo `id`.
- Injeção de dependência via campo (`@Autowired`) é o padrão adotado — não usar injeção por construtor.
- Nomes de campos de domínio e mensagens são em português (ex: `nome`, `email`, `qtdQuartos`, `responsavel`).

## Convenções de Resposta da API

| Operação | Status | Corpo |
|---|---|---|
| Leitura bem-sucedida | `200 OK` | DTO correspondente |
| Criação | `201 Created` + header `Location` | DTO criado |
| Atualização | `200 OK` | sem corpo |
| Deleção | `204 No Content` | sem corpo |
| Não encontrado | `404 Not Found` | `StandardError` |
| Erro de negócio | `400 Bad Request` | `StandardError` |
| Erro de validação | `422 Unprocessable Entity` | `ValidationError` com lista de `FieldMessage` |
