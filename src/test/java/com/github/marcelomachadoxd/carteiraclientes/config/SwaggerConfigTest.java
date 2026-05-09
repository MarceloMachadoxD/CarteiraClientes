package com.github.marcelomachadoxd.carteiraclientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de smoke do bean OpenAPI produzido pelo SwaggerConfig.
 * Valida: Requisitos 2.1, 2.2, 2.3, 2.4
 */
@SpringBootTest
@ActiveProfiles("test")
class SwaggerConfigTest {

    @Autowired
    private OpenAPI openAPI;

    /**
     * Verifica título, versão e descrição globais do bean OpenAPI.
     * Valida: Requisito 2.1
     */
    @Test
    void openAPI_deveConterMetadadosGlobaisCorretos() {
        assertNotNull(openAPI.getInfo(), "Info não deve ser nula");

        assertEquals("CarteiraClientes API", openAPI.getInfo().getTitle(),
                "Título deve ser 'CarteiraClientes API'");

        assertEquals("1.0.0", openAPI.getInfo().getVersion(),
                "Versão deve ser '1.0.0'");

        String descricao = openAPI.getInfo().getDescription();
        assertNotNull(descricao, "Descrição não deve ser nula");
        assertFalse(descricao.isBlank(), "Descrição não deve ser vazia");
        assertTrue(descricao.contains("corretores de imóveis"),
                "Descrição deve conter 'corretores de imóveis'");
        assertTrue(descricao.contains("carteira de clientes"),
                "Descrição deve conter 'carteira de clientes'");
    }

    /**
     * Verifica que o SecurityScheme 'bearerAuth' está configurado como HTTP/bearer/JWT.
     * Valida: Requisito 2.2
     */
    @Test
    void openAPI_deveConterSecuritySchemeBearerAuth() {
        assertNotNull(openAPI.getComponents(), "Components não deve ser nulo");
        assertNotNull(openAPI.getComponents().getSecuritySchemes(),
                "SecuritySchemes não deve ser nulo");

        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(bearerAuth, "SecurityScheme 'bearerAuth' deve estar presente");

        assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType(),
                "Tipo do SecurityScheme deve ser HTTP");
        assertEquals("bearer", bearerAuth.getScheme(),
                "Scheme deve ser 'bearer'");
        assertEquals("JWT", bearerAuth.getBearerFormat(),
                "BearerFormat deve ser 'JWT'");
    }

    /**
     * Verifica que o SecurityRequirement global com 'bearerAuth' está aplicado.
     * Valida: Requisito 2.3
     */
    @Test
    void openAPI_deveConterSecurityRequirementGlobal() {
        assertNotNull(openAPI.getSecurity(), "Lista de SecurityRequirements não deve ser nula");
        assertFalse(openAPI.getSecurity().isEmpty(),
                "Deve haver ao menos um SecurityRequirement global");

        boolean temBearerAuth = openAPI.getSecurity().stream()
                .anyMatch(req -> req.containsKey("bearerAuth"));

        assertTrue(temBearerAuth,
                "Deve existir SecurityRequirement global com 'bearerAuth'");
    }

    /**
     * Verifica que as respostas globais 400, 401 e 500 estão definidas nos components.
     * Valida: Requisito 2.4
     */
    @Test
    void openAPI_deveConterRespostasGlobaisDeErro() {
        assertNotNull(openAPI.getComponents(), "Components não deve ser nulo");
        assertNotNull(openAPI.getComponents().getResponses(),
                "Responses nos components não deve ser nulo");

        var responses = openAPI.getComponents().getResponses();

        assertTrue(responses.containsKey("400"),
                "Deve existir resposta global para código 400");
        assertTrue(responses.containsKey("401"),
                "Deve existir resposta global para código 401");
        assertTrue(responses.containsKey("500"),
                "Deve existir resposta global para código 500");

        assertNotNull(responses.get("400").getDescription(),
                "Resposta 400 deve ter descrição");
        assertNotNull(responses.get("401").getDescription(),
                "Resposta 401 deve ter descrição");
        assertNotNull(responses.get("500").getDescription(),
                "Resposta 500 deve ter descrição");
    }
}
