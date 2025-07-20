package com.avmsistemas.minha_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Produtos e Saudações")
                        .version("1.0")
                        .description("Esta é uma API de exemplo para gerenciamento de produtos e saudações."));
    }
}
