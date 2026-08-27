package com.ecocycle.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao do Swagger/OpenAPI - gera automaticamente a documentacao
 * interativa da API a partir dos controllers, DTOs e anotacoes de validacao
 * (Bean Validation) ja existentes no codigo. Serve como o "contrato" formal
 * entre back-end e front-end (Flutter), sempre sincronizado com o codigo real.
 *
 * Acessivel em /swagger-ui.html apos subir a aplicacao.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI ecoCycleOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EcoCycle Backend API")
                        .description(
                                "API REST do EcoCycle - app gamificado de educacao e descarte "
                                + "responsavel de lixo eletronico (TCC - Mackenzie). "
                                + "Cobre autenticacao, conteudo educativo/quizzes, album de "
                                + "figurinhas, pontos de coleta e scanner de componentes."
                        )
                        .version("v1")
                        .contact(new Contact()
                                .name("EcoCycle - Equipe de desenvolvimento")
                        )
                )
                // Define o esquema "bearerAuth" e o aplica globalmente. Endpoints
                // publicos (ex.: /auth/register, /auth/login) mostram o cadeado mas
                // simplesmente nao exigem token para funcionar - e so uma marca visual
                // no Swagger UI, nao afeta o comportamento real da API.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description(
                                        "Cole aqui APENAS o token (sem o prefixo 'Bearer '), "
                                        + "obtido em /api/v1/auth/register ou /api/v1/auth/login."
                                )
                        )
                );
    }
}
