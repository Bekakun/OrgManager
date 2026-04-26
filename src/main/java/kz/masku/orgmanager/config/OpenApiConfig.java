package kz.masku.orgmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers a Bearer-token security scheme so every Swagger UI endpoint
 * can be tested with a JWT obtained from POST /api/auth/login.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "Bearer Auth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OrgManager API")
                        .description("HR, RBAC and Document Workflow management system")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("OrgManager Team")
                                .email("dev@orgmanager.kz")))
                // Apply JWT auth globally to all endpoints in Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
