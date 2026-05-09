package com.github.marcelomachadoxd.carteiraclientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI carteiraClientesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CarteiraClientes API")
                        .description("API REST para corretores de imóveis gerenciarem carteira de clientes")
                        .version("1.0.0"));
    }
}
