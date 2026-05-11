package com.github.marcelomachadoxd.carteiraclientes.config;

import com.github.marcelomachadoxd.carteiraclientes.resources.exceptions.StandardError;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI carteiraClientesOpenAPI() {

        // Schema $ref para StandardError nos components
        Schema<StandardError> standardErrorSchema = new Schema<StandardError>()
                .$ref("#/components/schemas/StandardError");

        // Respostas de erro globais
        ApiResponse response400 = new ApiResponse()
                .description("Requisição inválida ou erro de negócio")
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/json",
                                new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(standardErrorSchema)));

        ApiResponse response401 = new ApiResponse()
                .description("Não autorizado — token JWT ausente ou inválido")
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/json",
                                new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(standardErrorSchema)));

        ApiResponse response500 = new ApiResponse()
                .description("Erro interno do servidor")
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/json",
                                new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(standardErrorSchema)));

        return new OpenAPI()
                .info(new Info()
                        .title("CarteiraClientes API")
                        .description("API REST para corretores de imóveis gerenciarem sua carteira de clientes de forma eficiente.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addResponses("400", response400)
                        .addResponses("401", response401)
                        .addResponses("500", response500));
    }
}
