##CarteiraDeClientes

Projeto criado para fins de estudo para o meu TCC que visa a criação de um CRUD para Registro e consultas de Clientes , Visitas e Responsável pela visita afim de auxiliar corretores de imóveis na gestão de clientes com perfil de interesse e visitas realizadas.

Tecnologias Utilizadas:
+ [Spring Boot](https://spring.io/projects/spring-boot)
+ [Spring Data JPA](https://spring.io/projects/spring-data)
+ [Maven](https://maven.apache.org/)
+ [H2 Database](https://www.h2database.com/)
+ [Swagger](https://swagger.io/)
+ [Testes com JUnit](https://junit.org/)

Coleções de exemplo para as requisições no postman disponíveis na pasta src/main/resources/postman-requests

para acessar o swagger e ter a documentação API dos endpoints e modelos de dados dos DTOs para entrada e saída de requisições disponíveis no link http://localhost:8080/swagger-ui.html

Para executar o projeto pela IDE(Sugestão Intellij) abra como projeto maven e execute a classe CarteiraDeClientesApplication.java

## Executando pelo terminal

### Pré-requisitos
+ Java 11 instalado e configurado no `PATH`
+ Nenhuma instalação adicional do Maven é necessária — o projeto inclui o Maven Wrapper (`mvnw.cmd`)

### Passos

**1. Clone o repositório e entre na pasta do projeto:**
```cmd
git clone https://github.com/marcelomachadoxd/CarteiraClientes.git
cd CarteiraClientes
```

**2. Compile o projeto (sem rodar os testes):**
```cmd
.\mvnw.cmd clean package -DskipTests
```

**3. Inicie a aplicação:**
```cmd
.\run.cmd
```

O script `run.cmd` verifica se a porta `8080` está ocupada antes de subir:
- Se estiver livre → sobe normalmente na `8080`
- Se o próprio projeto já estiver rodando → encerra e reinicia
- Se outro processo estiver usando a porta → pergunta qual porta usar (5 segundos para responder; sem resposta, usa a `9090`)

Para subir diretamente sem o script:
```cmd
.\mvnw.cmd spring-boot:run
```

A aplicação sobe com o perfil `test` ativo por padrão, usando banco H2 em memória com dados de seed carregados automaticamente.

### Acessos após subir

| Recurso | URL |
|---|---|
| Swagger UI (documentação da API) | http://localhost:8080/swagger-ui.html |
| H2 Console (banco em memória) | http://localhost:8080/h2-console |

> **H2 Console:** JDBC URL `jdbc:h2:mem:testdb`, usuário `sa`, senha em branco.

### Outros comandos úteis

```cmd
# Rodar os testes
.\mvnw.cmd test

# Verificar cobertura de código (mínimo 70% de linhas — relatório em target/site/jacoco/index.html)
.\mvnw.cmd verify
```