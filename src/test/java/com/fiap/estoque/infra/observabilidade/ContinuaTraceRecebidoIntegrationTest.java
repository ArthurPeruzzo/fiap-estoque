package com.fiap.estoque.infra.observabilidade;

import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O estoque continua o trace enviado pelo chamador (cabeçalho W3C {@code traceparent}): os logs estruturados
 * (ECS) da requisição carregam o MESMO trace id, então logs e traces dos dois serviços se correlacionam.
 */
@ActiveProfiles("integration-test")
@TestPropertySource(properties = "management.tracing.sampling.probability=1.0")
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContinuaTraceRecebidoIntegrationTest extends AbstractContainer {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void shouldLogarComOTraceIdRecebidoNoCabecalho(CapturedOutput output) {
        // Token inválido de propósito: a rejeição gera um log WARN dentro da requisição.
        RestAssured.given()
                .header("traceparent", "00-" + TRACE_ID + "-00f067aa0ba902b7-01")
                .header("Authorization", "Bearer token-invalido")
                .when().post("/reserva/ordem-servico/7/liberar")
                .then().statusCode(401);

        assertThat(output.getAll())
                .as("o log da requisição deveria carregar o trace id enviado pelo chamador")
                .contains(TRACE_ID);
    }

    @Test
    void shouldGerarUmTraceNovoQuandoNaoHaCabecalho(CapturedOutput output) {
        RestAssured.given()
                .header("Authorization", "Bearer token-invalido")
                .when().post("/reserva/ordem-servico/7/liberar")
                .then().statusCode(401);

        assertThat(output.getAll()).doesNotContain(TRACE_ID);
    }
}
