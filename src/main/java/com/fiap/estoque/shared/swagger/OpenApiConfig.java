package com.fiap.estoque.shared.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        tags = {
                @Tag(name = "Peça", description = "Operações relacionadas as peças"),
                @Tag(name = "Insumo", description = "Operações relacionadas aos insumos")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

        @Bean
        public SwaggerUiConfigParameters swaggerUiConfig(SwaggerUiConfigProperties properties) {
                var config = new SwaggerUiConfigParameters(properties);
                config.setTagsSorter(null);
                return config;
        }

        /**
         * Declara explicitamente a URL publica da API no documento OpenAPI.
         *
         * <p>Sem isso, o springdoc deduz o campo {@code servers} a partir do header {@code Host} da
         * requisicao. Atras do API Gateway (integracao {@code HTTP_PROXY}) esse header carrega o host
         * do <em>alvo</em> — o ELB do cluster —, e nao o do {@code execute-api}. O resultado e um
         * Swagger que carrega pelo gateway mas cujo "Try it out" dispara contra o ELB, por fora dele.
         *
         * <p>Quando {@code swagger.server-url} esta vazio (dev, docker compose, testes), nada e
         * sobrescrito e a deducao automatica do springdoc — que ali esta correta — e preservada.
         */
        @Bean
        public OpenApiCustomizer serverUrlCustomizer(@Value("${swagger.server-url:}") String serverUrl) {
                return openApi -> {
                        if (!serverUrl.isBlank()) {
                                openApi.setServers(List.of(new Server().url(serverUrl).description("API Gateway")));
                        }
                };
        }

}


