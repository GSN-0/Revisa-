package com.revisa.revisa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI revisaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Revisa API")
                        .version("1.0")
                        .description("API para planejamento de estudos com revisão espaçada."))
                .schemaRequirement(BEARER_AUTH, new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
