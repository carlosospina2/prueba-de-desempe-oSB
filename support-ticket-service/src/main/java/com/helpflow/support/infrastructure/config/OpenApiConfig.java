package com.helpflow.support.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the "bearerAuth" HTTP Bearer JWT security scheme so that
 * Swagger UI shows the global Authorize button and allows sending
 * Authorization: Bearer <token> on protected endpoints.
 *
 * This class only configures OpenAPI documentation -- it does not change
 * any Spring Security rules.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HelpFlow Support Ticket API")
                        .description("Sistema de Gestion de Tickets de Soporte TI - HelpFlow")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HelpFlow Team")
                                .email("soporte@helpflow.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido en POST /auth/login")));
    }
}