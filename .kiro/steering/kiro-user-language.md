---
inclusion: always
---

# Idioma e Comunicação

## Regra Principal

**Toda comunicação com o usuário deve ser em português brasileiro.** Isso inclui perguntas, explicações, resumos, mensagens de progresso, descrições de requisitos, critérios de aceitação, histórias de usuário, conteúdo de documentos de spec (`requirements.md`, `design.md`, `tasks.md`) e mensagens de erro.

## Exceções — Manter em Inglês

Os elementos abaixo **devem permanecer em inglês**, seguindo convenções da comunidade de desenvolvimento:

| Categoria | Exemplos |
|---|---|
| Identificadores de código (classes, métodos, campos, pacotes) | `ClienteService`, `findById`, `pageable`, `@RestController` |
| Trechos de código e snippets | qualquer bloco de código-fonte |
| Comandos de terminal e flags CLI | `mvnw.cmd test`, `-Dtest=ClassName` |
| Termos técnicos consagrados | `endpoint`, `payload`, `bean`, `mock`, `stub`, `property-based testing` |
| Frameworks, bibliotecas e ferramentas | Spring Boot, JaCoCo, jqwik, Mockito, Hibernate |
| Caminhos e nomes de arquivos | `pom.xml`, `application-test.properties` |

## Mistura de Idiomas em Documentos de Spec

Ao redigir documentos de spec, misture os idiomas conforme necessário: prosa e narrativa em português, identificadores de código em inglês inline.

**Correto:**
> "Quando uma requisição `GET /clientes/id/{id}` é feita com um id existente, o `ClienteResource` deve responder com HTTP 200 e o `ClienteDTO` correspondente no corpo."

**Incorreto:**
> "When a GET request is made to /clientes/id/{id} with an existing id, the resource should return 200."

## Cabeçalhos Estruturais Obrigatórios em Inglês (requirements.md)

A ferramenta de análise automática de requisitos (`analyzeRequirements`) exige que os cabeçalhos estruturais do `requirements.md` estejam **obrigatoriamente em inglês**, independentemente do idioma do conteúdo. Use sempre:

| Cabeçalho obrigatório (inglês) | Equivalente em português (NÃO usar como cabeçalho) |
|---|---|
| `## Introduction` | ~~Introdução~~ |
| `## Requirements` | ~~Requisitos~~ |
| `### Requirement N: [Título]` | ~~Requisito N~~ |
| `**User Story:**` | ~~História de Usuário~~ |
| `#### Acceptance Criteria` | ~~Critérios de Aceitação~~ |

O conteúdo dentro dessas seções (introdução, descrição dos requisitos, critérios, histórias de usuário) deve continuar em **português brasileiro**. Apenas os cabeçalhos e rótulos estruturais devem ser em inglês.

**Correto:**
```markdown
## Introduction

Atualização da stack do projeto para Java 25 e Spring Boot 4.0.0...

## Requirements

### Requirement 1: Atualização do Build System

**User Story:** Como desenvolvedor, quero que o `pom.xml` use Java 25...

#### Acceptance Criteria

1. THE **Build_System** SHALL declarar `<java.version>25</java.version>`...
```

**Incorreto:**
```markdown
## Introdução
## Requisitos
### Requisito 1: Atualização do Build System
#### Critérios de Aceitação
```
