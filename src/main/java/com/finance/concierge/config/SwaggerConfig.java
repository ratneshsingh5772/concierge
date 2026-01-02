package com.finance.concierge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * Provides API documentation at /swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }

    /**
     * API Information
     */
    private Info apiInfo() {
        return new Info()
                .title("Finance Concierge API")
                .description("Personal Finance Concierge - AI-powered expense tracking and budget management system. " +
                            "Track your spending via natural language, manage budgets, and get intelligent financial insights.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Finance Concierge Team")
                        .email("support@financeconcierge.com")
                        .url("https://github.com/yourusername/finance-concierge"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * API Servers
     */
    private List<Server> apiServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://api.financeconcierge.com")
                .description("Production Server");

        return List.of(localServer, productionServer);
    }

    /**
     * Security Components (JWT)
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token (without 'Bearer' prefix)")
                );
    }

    /**
     * Security Requirement
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }
}

