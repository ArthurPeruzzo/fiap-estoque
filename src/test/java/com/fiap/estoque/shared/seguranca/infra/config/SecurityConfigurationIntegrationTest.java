package com.fiap.estoque.shared.seguranca.infra.config;

import com.fiap.estoque.resources.TokenTestHelper;
import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integration-test")
@Import(TokenTestHelper.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigurationIntegrationTest extends AbstractContainer {

    @Autowired
    private TokenTestHelper tokenTestHelper;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void shouldReturn401WhenNoToken() {
        RestAssured.given().when().get("/peca").then().statusCode(401);
        RestAssured.given().when().get("/insumo").then().statusCode(401);
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() {
        RestAssured.given().header("Authorization", "Bearer token.invalido.aqui")
                .when().get("/peca").then().statusCode(401);
    }

    @Test
    void shouldReturn403WhenRoleIsNotAdministrador() {
        String token = tokenTestHelper.gerar("ROLE_MECANICO");

        RestAssured.given().header("Authorization", "Bearer " + token)
                .when().get("/peca").then().statusCode(403);
    }

    @Test
    void shouldReturn200WhenRoleIsAdministrador() {
        RestAssured.given().header("Authorization", "Bearer " + tokenTestHelper.administrador())
                .when().get("/peca").then().statusCode(200);
    }

    @Test
    void shouldDenyUnmappedEndpoints() {
        RestAssured.given().header("Authorization", "Bearer " + tokenTestHelper.administrador())
                .when().get("/qualquer-outro").then().statusCode(403);
    }

    @Test
    void shouldExposeHealthWithoutToken() {
        RestAssured.given().when().get("/actuator/health").then().statusCode(200);
    }
}
