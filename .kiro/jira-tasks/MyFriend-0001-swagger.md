# [Docs] Enriquecer Swagger com Casos de Uso e Exemplos de Negócio

**Contexto:**
O projeto foi criado inicialmente como requisito para um TCC. Agora,para expandir sua entrega em um produto funcional, precisamos evoluir a documentação das APIs. Atualmente, possuímos uma documentação básica no Swagger. O objetivo é transformá-la em um guia de uso integrado e prático, evitando a necessidade de documentações externas (wikis). A documentação deve guiar o consumidor da API através de exemplos claros e alinhados com as regras do nosso negócio.

**O que fazer (Abordagem Técnica):**
A documentação não deve poluir o código de negócio atual. Para manter a base limpa, a implementação deve seguir uma arquitetura de documentação em duas camadas:
1. **Interfaces Contratuais:** Para cada Controller da aplicação, criar uma Interface correspondente (ex: `ClienteControllerDocs` ou `IClienteController`). Todas as anotações específicas de endpoint do OpenAPI/Swagger (`@Operation`, `@Parameter`, exemplos de payloads) devem residir *exclusivamente* nesta interface. Os Controllers atuais passarão apenas a implementar essas interfaces.
2. **Configuração Centralizada:** Para evitar repetição de código, criar uma classe de configuração global do OpenAPI. Nela devem ser definidos programaticamente os elementos comuns a todos os endpoints, como: metadados globais da API, esquemas de segurança (ex: tokens) e respostas HTTP de erro padrão (como 400 Bad Request, 401 Unauthorized e 500 Internal Server Error).

**Escopo e Casos de Uso Mínimos:**
* **Domínio de Clientes:** Documentar o cadastro de clientes e criar exemplos de busca para encontrar grupos de clientes com base em suas preferências.
* **Domínio de Imóveis:** Documentar e exemplificar chamadas que buscam imóveis com base no "match" das preferências de um cliente.
* **Demais Funcionalidades:** Revisar todos os outros endpoints expostos pelos controllers e garantir que possuam dados de exemplo verossímeis.

**Critérios de Aceite (DoD):**
1. Os Controllers estão limpos e não possuem anotações de documentação (Swagger) em seus métodos; toda a doc está delegada às Interfaces.
2. Códigos de erro globais estão configurados de forma centralizada no Bean do OpenAPI e removidos/omitidos das anotações individuais dos endpoints.
3. Todos os endpoints públicos possuem descrições detalhadas (`summary` e `description`) nas interfaces.
4. Os schemas de *Request* e *Response* contêm propriedades de `example` com dados mockados coerentes com o negócio (evitar genéricos; usar "Apartamento 2 quartos", "Cliente VIP").
5. O build e os testes da aplicação passam com sucesso nos hooks automatizados, garantindo que a extração para interfaces não quebrou o roteamento do framework.