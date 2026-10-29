package com.fiap.estoque.infra.controller;

import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogoIntegrationTest extends AbstractContainer {

    @Autowired private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        jdbcTemplate.update("INSERT INTO peca (id, nome, descricao, preco, quantidade_estoque, quantidade_reservada) VALUES (1, 'Filtro', 'desc', 42.00, 10, 0)");
        jdbcTemplate.update("INSERT INTO peca (id, nome, descricao, preco, quantidade_estoque, quantidade_reservada) VALUES (2, 'Pastilha', 'desc', 89.90, 10, 0)");
        jdbcTemplate.update("INSERT INTO insumo (id, nome, descricao, preco, quantidade_estoque, quantidade_reservada, unidade_medida) VALUES (1, 'Óleo', 'desc', 38.00, 10, 0, 'LITRO')");
    }

    @Test
    void shouldConsultarPecasEmLoteSemAutenticacaoOmitindoIdsInexistentes() {
        RestAssured.given()
                .when().get("/catalogo/pecas?ids=1,2,999")
                .then().statusCode(200)
                .body("size()", Matchers.equalTo(2))
                .body("id", Matchers.containsInAnyOrder(1, 2))
                .body("find { it.id == 2 }.preco", Matchers.equalTo(89.9f));
    }

    @Test
    void shouldConsultarInsumosEmLote() {
        RestAssured.given()
                .when().get("/catalogo/insumos?ids=1")
                .then().statusCode(200)
                .body("size()", Matchers.equalTo(1))
                .body("[0].id", Matchers.equalTo(1))
                .body("[0].preco", Matchers.equalTo(38.0f));
    }

    @Test
    void shouldReturnListaVaziaQuandoNenhumIdExiste() {
        RestAssured.given()
                .when().get("/catalogo/pecas?ids=999")
                .then().statusCode(200)
                .body("size()", Matchers.equalTo(0));
    }

    @Test
    void shouldReturn400SemIds() {
        RestAssured.given()
                .when().get("/catalogo/pecas")
                .then().statusCode(400);
    }
}
