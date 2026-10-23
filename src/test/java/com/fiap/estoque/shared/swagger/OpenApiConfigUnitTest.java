package com.fiap.estoque.shared.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigUnitTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    @DisplayName("Deve declarar a URL publica como unico server quando a propriedade esta preenchida")
    void deveDeclararUrlPublicaQuandoPropriedadePreenchida() {
        String url = "https://ovk99xx9dl.execute-api.us-east-1.amazonaws.com";
        OpenAPI openApi = new OpenAPI();

        openApiConfig.serverUrlCustomizer(url).customise(openApi);

        assertThat(openApi.getServers())
                .singleElement()
                .satisfies(server -> {
                    assertThat(server.getUrl()).isEqualTo(url);
                    assertThat(server.getDescription()).isEqualTo("API Gateway");
                });
    }

    @Test
    @DisplayName("Nao deve sobrescrever os servers quando a propriedade esta vazia")
    void naoDeveSobrescreverServersQuandoPropriedadeVazia() {
        Server deduzidoPeloSpringdoc = new Server().url("http://localhost:8080");
        OpenAPI openApi = new OpenAPI().servers(List.of(deduzidoPeloSpringdoc));

        openApiConfig.serverUrlCustomizer("").customise(openApi);

        assertThat(openApi.getServers()).containsExactly(deduzidoPeloSpringdoc);
    }

    @Test
    @DisplayName("Nao deve sobrescrever os servers quando a propriedade contem apenas espacos")
    void naoDeveSobrescreverServersQuandoPropriedadeEmBranco() {
        OpenAPI openApi = new OpenAPI();

        openApiConfig.serverUrlCustomizer("   ").customise(openApi);

        assertThat(openApi.getServers()).isNull();
    }
}
