package com.shelflife.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("ShelfLife API")
                        .description("API for ShelfLife backend services")
                        .version("v1.0")
                        .contact(new Contact().name("ShelfLife Team"))
                        .license(new License().name("MIT")))
                .externalDocs(new ExternalDocumentation().description("Project README").url("https://example.com"));
    }
}
