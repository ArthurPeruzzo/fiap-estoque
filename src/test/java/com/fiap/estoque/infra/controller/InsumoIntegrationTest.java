package com.fiap.estoque.infra.controller;

import com.fiap.estoque.infra.gateway.repository.InsumoRepository;
import com.fiap.estoque.resources.TokenTestHelper;
import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("integration-test")
@Import(TokenTestHelper.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InsumoIntegrationTest extends AbstractContainer {

    @Autowired
    private TokenTestHelper tokenTestHelper;

    @Autowired
    private InsumoRepository insumoRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private String obterToken() {
        return tokenTestHelper.administrador();
    }

    // -------------------------------------------------------------------------
    // POST /insumo
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateInsumoSuccessfully() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo de motor","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(201);

        var insumos = insumoRepository.findAll();
        Assertions.assertEquals(1, insumos.size());
        Assertions.assertEquals("Óleo de motor", insumos.getFirst().getNome());
        Assertions.assertEquals(10, insumos.getFirst().getQuantidadeEstoque());
    }

    @Test
    void shouldCreateInsumoWithAllUnidadeMedidaValues() {
        String token = obterToken();

        for (String unidade : List.of("LITRO", "ML", "UNIDADE")) {
            RestAssured
                    .given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(String.format(
                            "{\"nome\":\"Item %s\",\"descricao\":\"Desc\",\"preco\":10.00,\"quantidadeEstoque\":5,\"unidadeMedida\":\"%s\"}",
                            unidade, unidade))
                    .when()
                    .post("/insumo")
                    .then()
                    .statusCode(201);
        }

        Assertions.assertEquals(3, insumoRepository.findAll().size());
    }

    @Test
    void shouldCreateInsumoWithZeroEstoque() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Fluido de freio","descricao":"DOT 4","preco":25.00,"quantidadeEstoque":0,"unidadeMedida":"ML"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(201);

        Assertions.assertEquals(0, insumoRepository.findAll().getFirst().getQuantidadeEstoque());
    }

    @Test
    void shouldReturn400WhenNomeIsBlank() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(400)
                .body("nome", Matchers.notNullValue());
    }

    @Test
    void shouldReturn400WhenPrecoIsZero() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo","descricao":"Óleo 5W30","preco":0.00,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(400)
                .body("preco", Matchers.equalTo("O preço deve ser maior que zero"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {"nome":"Óleo","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(401);
    }

    // -------------------------------------------------------------------------
    // GET /insumo
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnEmptyPageWhenNoInsumosCadastrados() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/insumo")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(0))
                .body("totalElements", Matchers.equalTo(0))
                .body("totalPages", Matchers.equalTo(0))
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(10));
    }

    @Test
    void shouldReturnInsumosPagedAfterCreation() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo de motor","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when().post("/insumo").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Fluido de freio","descricao":"DOT 4","preco":25.00,"quantidadeEstoque":5,"unidadeMedida":"ML"}
                        """)
                .when().post("/insumo").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/insumo")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(2))
                .body("totalElements", Matchers.equalTo(2))
                .body("totalPages", Matchers.equalTo(1))
                .body("content[0].nome", Matchers.notNullValue())
                .body("content[0].unidadeMedida", Matchers.notNullValue());
    }

    @Test
    void shouldRespectPageSizeInPagination() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo de motor","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when().post("/insumo").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Fluido de freio","descricao":"DOT 4","preco":25.00,"quantidadeEstoque":5,"unidadeMedida":"ML"}
                        """)
                .when().post("/insumo").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/insumo")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("totalElements", Matchers.equalTo(2))
                .body("totalPages", Matchers.equalTo(2))
                .body("size", Matchers.equalTo(1));
    }

    @Test
    void shouldUpdateInsumoSuccessfully() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo de motor","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when().post("/insumo").then().statusCode(201);

        Long id = insumoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Fluido de freio","descricao":"DOT 4","preco":25.00,"quantidadeEstoque":500,"unidadeMedida":"ML"}
                        """)
                .when()
                .put("/insumo/" + id)
                .then()
                .statusCode(204);

        var updated = insumoRepository.findById(id).orElseThrow();
        Assertions.assertEquals("Fluido de freio", updated.getNome());
        Assertions.assertEquals("DOT 4", updated.getDescricao());
        Assertions.assertEquals(0, new java.math.BigDecimal("25.00").compareTo(updated.getPreco()));
        Assertions.assertEquals(500, updated.getQuantidadeEstoque());
        Assertions.assertEquals(com.fiap.estoque.core.domain.UnidadeMedida.ML, updated.getUnidadeMedida());
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnUpdate() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo","descricao":"5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .put("/insumo/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Insumo não encontrado"));
    }

    @Test
    void shouldReturn400WhenUnidadeMedidaIsMissingOnUpdate() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo de motor","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when().post("/insumo").then().statusCode(201);

        Long id = insumoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo","descricao":"5W30","preco":45.90,"quantidadeEstoque":10}
                        """)
                .when()
                .put("/insumo/" + id)
                .then()
                .statusCode(400)
                .body("unidadeMedida", Matchers.notNullValue());
    }

    @Test
    void shouldDeleteInsumoSuccessfully() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo de motor","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when().post("/insumo").then().statusCode(201);

        Long id = insumoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/insumo/" + id)
                .then()
                .statusCode(204);

        Assertions.assertTrue(insumoRepository.findById(id).isEmpty());
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDelete() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/insumo/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Insumo não encontrado"));
    }
}
