# Testing Guidelines

## jqwik + Spring (@DataJpaTest) Integration Pattern

### O Problema

jqwik cria sua própria instância da classe de teste **separada** da instância gerenciada pelo Spring. Isso significa que campos anotados com `@Autowired` ficam `null` dentro de métodos `@Property`. Tentar usar o repositório diretamente nesses métodos causa `NullPointerException`.

Esse problema foi resolvido com o padrão abaixo. **Toda classe `@DataJpaTest` que também contenha `@Property` deve seguir este padrão obrigatoriamente.**

### A Solução: ApplicationContextAware + TestContextManager

A classe de teste deve:

1. Implementar `ApplicationContextAware`
2. Manter um campo estático `sharedXxxRepository` populado pelo Spring via `setApplicationContext`
3. Expor um método `getRepository()` que retorna o repositório correto dependendo do contexto (JUnit ou jqwik)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class XxxRepositoryTest implements ApplicationContextAware {

    @Autowired
    private XxxRepository xxxRepository;

    // Compartilhado com instâncias jqwik (que não têm injeção Spring)
    private static XxxRepository sharedXxxRepository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        sharedXxxRepository = applicationContext.getBean(XxxRepository.class);
    }

    private XxxRepository getRepository() {
        if (xxxRepository != null) return xxxRepository;
        if (sharedXxxRepository != null) return sharedXxxRepository;
        // Fallback: inicializa contexto Spring para instância jqwik
        try {
            TestContextManager tcm = new TestContextManager(XxxRepositoryTest.class);
            tcm.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return xxxRepository;
    }

    // Métodos @Test usam xxxRepository diretamente (injetado pelo Spring)
    @Test
    void someTest() {
        xxxRepository.findAll(); // OK
    }

    // Métodos @Property usam getRepository() (funciona em ambos os contextos)
    @Property(tries = 50)
    void someProperty(@ForAll @IntRange(min=1, max=5) int value) {
        XxxRepository repo = getRepository(); // SEMPRE usar getRepository() em @Property
        repo.findAll(); // OK
    }
}
```

### Imports Necessários

```java
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestContextManager;
```

### Regras

- **Métodos `@Test`**: podem usar o campo `@Autowired` diretamente
- **Métodos `@Property`**: SEMPRE usar `getRepository()` — nunca o campo diretamente
- **`@AutoConfigureTestDatabase(replace = Replace.NONE)`**: obrigatório junto com `@DataJpaTest` para usar o H2 configurado no perfil `test` em vez do H2 padrão do slice
- **`repo.flush()`**: chamar após `repo.save()` em `@Property` para garantir que os dados estão visíveis na mesma transação antes de executar a query
- Usar `System.nanoTime()` ou UUID no email ao salvar entidades em `@Property` para evitar violação de constraint de unicidade entre as 50+ iterações

### Exemplo Real (ClienteRepositoryTest)

Ver `src/test/java/.../repositories/ClienteRepositoryTest.java` — implementação de referência completa com `findByNomePrefixMatchProperty` (Property 1) e `findByInteressesFilterCorrectnessProperty` (Property 2).

---

## Perfil de Teste

Todos os testes usam `@ActiveProfiles("test")`, que ativa o H2 in-memory via `application-test.properties`. O seed data (`data.sql`) é carregado automaticamente.

**Fatos do seed data usados nos testes:**
- Cliente id=1, nome='Cliente'
- User id=2 é responsável pelas visitas id=1..30
- Visita id=1 tem cliente_id=1 e responsavel_id=2
- Role id=1 existe

---

## Comandos Maven (Windows)

```cmd
# Rodar todos os testes
mvnw.cmd test

# Rodar uma classe específica
mvnw.cmd test -Dtest=ClienteRepositoryTest

# Rodar um método específico
mvnw.cmd test -Dtest=ClienteRepositoryTest#findByInteressesFilterCorrectnessProperty

# Verificar cobertura JaCoCo (≥ 70%)
mvnw.cmd verify
```
