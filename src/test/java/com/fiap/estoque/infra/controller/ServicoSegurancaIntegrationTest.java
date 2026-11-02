package com.fiap.estoque.infra.controller;

import com.fiap.estoque.resources.TokenTestHelper;
import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Autenticação serviço a serviço das rotas internas (/reserva/** e /catalogo/**): só o token de serviço do
 * fiap-mecanica, com o escopo da rota, é aceito. O JWT de usuário não vale nelas e vice-versa.
 */
@ActiveProfiles("integration-test")
@Import(TokenTestHelper.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServicoSegurancaIntegrationTest extends AbstractContainer {

    private static final String LIBERAR = "/reserva/ordem-servico/7/liberar";
    private static final String CATALOGO = "/catalogo/pecas?ids=1";

    @Autowired private TokenTestHelper tokens;

    @Value("${jwt.secret}")
    private String segredoDeUsuario;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private int postLiberar(String token) {
        var requisicao = RestAssured.given();
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        return requisicao.when().post(LIBERAR).statusCode();
    }

    private int getCatalogo(String token) {
        var requisicao = RestAssured.given();
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        return requisicao.when().get(CATALOGO).statusCode();
    }

    private void assertStatus(int esperado, int atual) {
        org.junit.jupiter.api.Assertions.assertEquals(esperado, atual);
    }

    @Test
    void shouldAceitarTokenDeServicoComEscopoDeReserva() {
        assertStatus(204, postLiberar(tokens.servicoReserva()));
    }

    @Test
    void shouldAceitarTokenDeServicoComEscopoDeCatalogo() {
        assertStatus(200, getCatalogo(tokens.servicoCatalogo()));
    }

    @Test
    void shouldAceitarTokenComVariosEscopos() {
        assertStatus(204, postLiberar(tokens.servico("catalogo:leitura", "reserva:escrita")));
    }

    @Test
    void shouldResponder401SemToken() {
        assertStatus(401, postLiberar(null));
        assertStatus(401, getCatalogo(null));
    }

    @Test
    void shouldResponder401ParaJwtDeUsuarioNasRotasInternas() {
        assertStatus(401, postLiberar(tokens.administrador()));
        assertStatus(401, getCatalogo(tokens.administrador()));
    }

    @Test
    void shouldResponder401ParaTokenDeServicoAssinadoComOSegredoDeUsuario() {
        String forjado = tokens.servico(tokens.audienciaDeServico(), Instant.now().plus(90, ChronoUnit.SECONDS),
                segredoDeUsuario, "reserva:escrita");

        assertStatus(401, postLiberar(forjado));
    }

    @Test
    void shouldResponder401ParaAudienciaDeOutroServico() {
        String token = tokens.servico("fiap-outro-servico", Instant.now().plus(90, ChronoUnit.SECONDS),
                tokens.segredoDeServico(), "reserva:escrita");

        assertStatus(401, postLiberar(token));
    }

    @Test
    void shouldResponder401ParaTokenExpirado() {
        String token = tokens.servico(tokens.audienciaDeServico(), Instant.now().minus(5, ChronoUnit.MINUTES),
                tokens.segredoDeServico(), "reserva:escrita");

        assertStatus(401, postLiberar(token));
    }

    @Test
    void shouldResponder403QuandoOEscopoNaoCorrespondeARota() {
        assertStatus(403, postLiberar(tokens.servicoCatalogo()));
        assertStatus(403, getCatalogo(tokens.servicoReserva()));
    }

    @Test
    void shouldResponder403ParaTokenDeServicoSemEscopo() {
        assertStatus(403, postLiberar(tokens.servico()));
    }

    @Test
    void shouldResponder401ParaTokenDeServicoNasRotasDeUsuario() {
        RestAssured.given()
                .header("Authorization", "Bearer " + tokens.servico("reserva:escrita", "catalogo:leitura"))
                .when().get("/peca")
                .then().statusCode(401);
    }

    @Test
    void shouldContinuarAceitandoJwtDeAdministradorNasRotasDeUsuario() {
        RestAssured.given()
                .header("Authorization", "Bearer " + tokens.administrador())
                .when().get("/peca")
                .then().statusCode(200);
    }
}
