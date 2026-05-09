---
inclusion: always
---

# CarteiraClientes — Visão Geral do Produto

API REST backend para corretores de imóveis gerenciarem sua carteira de clientes. Projeto acadêmico (TCC).

## Domínio Principal

| Entidade | Tabela DB | Descrição |
|---|---|---|
| `Cliente` | `tb_cliente` | Dados pessoais + perfil de interesse em imóveis |
| `Visita` | `tb_visitas` | Registro de visita a imóvel feita para um cliente |
| `User` | `tb_user` | Corretor/agente responsável pelas visitas |
| `Role` | `tb_role` | Papel de acesso atribuído a um `User` |

## Campos de Interesse do Cliente

O perfil de busca de um `Cliente` é composto por:

- `qtdQuartos` — número mínimo de quartos desejados
- `qtdBanheiros` — número mínimo de banheiros desejados
- `qtdVagas` — número mínimo de vagas de garagem desejadas
- `metragem` — área máxima aceitável (m²)
- `valorMaximo` — orçamento máximo (R$)
- `obs` — observações livres (TEXT)

## Relacionamentos

- `Cliente` possui uma lista de `Visita` (`@OneToMany`)
- `Visita` referencia um `Cliente` e um `User` responsável (`@ManyToOne`)
- `User` possui um `Set<Role>` (`@ManyToMany`, `EAGER`) e uma lista de `Visita`

## Endpoints Principais

| Método | Path | Descrição |
|---|---|---|
| `GET` | `/clientes/id/{id}` | Busca cliente por ID |
| `GET` | `/clientes/nome/{nome}` | Busca clientes por prefixo de nome (paginado) |
| `GET` | `/clientes?margem=&qtdQuartos=&...` | Filtra clientes por perfil de interesse com margem de tolerância |
| `POST` | `/clientes` | Cria novo cliente |
| `PUT` | `/clientes/id/{id}` | Atualiza cliente |
| `DELETE` | `/clientes/id/{id}` | Remove cliente |

Endpoints análogos existem para `Visita`, `User` e `Role`.

## Lógica de Filtragem por Interesses

A query `findByInteresses` em `ClienteRepository` aplica uma margem percentual (`margem`) sobre `valorMaximo` e `metragem`. Parâmetros com valor `0` são ignorados (sem filtro para aquele campo). Essa semântica deve ser preservada em qualquer alteração na query ou no serviço.

## Convenções de Resposta da API

- Respostas de lista são sempre `Page<DTO>` com `Pageable`
- Criação retorna `201 Created` com `Location` header
- Atualização retorna `200 OK` sem corpo
- Deleção retorna `204 No Content`
- Erros de negócio: `ResourceNotFoundException` → 404, `DatabaseException` → 400
- Erros de validação: `ValidationError` com lista de `FieldMessage` por campo

## Acesso e Documentação

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console (perfil `test`): `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:testdb`, user: `sa`, sem senha)
- Coleções Postman: `src/main/resources/postman-requests/`
