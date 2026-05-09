---
inclusion: fileMatch
fileMatchPattern: ['**/test/**/*.java', '**/repositories/**', '**/services/**', '**/resources/**']
---

# Diretrizes de Testes

## Perfil de Teste

Todos os testes usam `@ActiveProfiles("test")`, que ativa H2 in-memory via `application-test.properties`. O seed data (`data.sql`) é carregado automaticamente na inicialização.

**Fatos do seed data relevantes para testes:**
- `Cliente` id=1, nome=`'Cliente'`
- `User` id=2 é responsável pelas `Visita` id=1..30
- `Visita` id=1 tem `cliente_id=1` e `responsavel_id=2`
- `Role` id=1 existe

---

## Padrão Obrigatório: jqwik + Spring (`@DataJpaTest`)

### Problema

jqwik instancia a classe de teste **separadamente** do Spring, fazendo com que campos `@Autowired` fiquem `null` dentro de métodos `@Property`, causando `NullPointerException`.

### Solução: `ApplicationContextAware` + `TestContextManager`

**Toda classe `@DataJpaTest` que contenha métodos `@Property` DEVE seguir este padrão:**

1. Implementar `ApplicationContextAware`
2. Manter um campo estático `sharedXxxRepository` populado via `setApplicationContext`
3. Expor `getRepository()` que resolve o repositório correto conforme o contexto (JUnit ou jqwik)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class XxxRepositoryTest implements ApplicationContextAware {

    @Autowired
    private XxxRepository xxxRepository;

    private static XxxRepository sharedXxxRepository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        sharedXxxRepository = applicationContext.getBean(XxxRepository.class);
    }

    private XxxRepository getRepository() {
        if (xxxRepository != null) return xxxRepository;
        if (sharedXxxRepository != null) return sharedXxxRepository;
        try {
            TestContextManager tcm = new TestContextManager(XxxRepositoryTest.class);
            tcm.prepareTestInstance(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spring context for property test", e);
        }
        return xxxRepository;
    }

    @Test
    void someTest() {
        xxxRepository.findAll(); // @Test: usar campo @Autowired diretamente
    }

    @Property(tries = 50)
    void someProperty(@ForAll @IntRange(min = 1, max = 5) int value) {
        XxxRepository repo = getRepository(); // @Property: SEMPRE usar getRepository()
        repo.findAll();
    }
}
```

### Imports necessários

```java
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestContextManager;
```

### Regras críticas

| Contexto | Como acessar o repositório |
|---|---|
| Métodos `@Test` | Campo `@Autowired` diretamente |
| Métodos `@Property` | **Sempre** via `getRepository()` |

- **`@AutoConfigureTestDatabase(replace = Replace.NONE)`** é obrigatório em todo `@DataJpaTest` para usar o H2 do perfil `test` em vez do H2 padrão do slice.
- Chamar **`repo.flush()`** após `repo.save()` em `@Property` para garantir visibilidade dos dados na mesma transação antes de executar queries.
- Usar **`System.nanoTime()` ou UUID** em campos únicos (ex: email) ao salvar entidades em `@Property`, evitando violações de constraint de unicidade entre as iterações.

### Referência de implementação

`ClienteRepositoryTest` é a implementação de referência completa, contendo `findByNomePrefixMatchProperty` e `findByInteressesFilterCorrectnessProperty`.

---

## Comandos Maven (Windows)

```cmd
# Rodar todos os testes
mvnw.cmd test

# Rodar uma classe específica
mvnw.cmd test -Dtest=ClienteRepositoryTest

# Rodar um método específico
mvnw.cmd test -Dtest=ClienteRepositoryTest#findByInteressesFilterCorrectnessProperty

# Verificar cobertura JaCoCo (mínimo 70% de linhas)
mvnw.cmd verify
```
