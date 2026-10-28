package com.fiap.estoque.infra.controller;

import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservaIntegrationTest extends AbstractContainer {

    private static final Long ORDEM_ID = 7L;
    private static final String URL = "/reserva/ordem-servico/" + ORDEM_ID;

    @Autowired private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private void criarPeca(long id, int estoque) {
        jdbcTemplate.update("INSERT INTO peca (id, nome, descricao, preco, quantidade_estoque, quantidade_reservada) VALUES (?, 'Filtro', 'desc', 10.00, ?, 0)", id, estoque);
    }

    private void criarInsumo(long id, int estoque) {
        jdbcTemplate.update("INSERT INTO insumo (id, nome, descricao, preco, quantidade_estoque, quantidade_reservada, unidade_medida) VALUES (?, 'Óleo', 'desc', 10.00, ?, 0, 'LITRO')", id, estoque);
    }

    private int pecaReservada(long id) {
        return jdbcTemplate.queryForObject("SELECT quantidade_reservada FROM peca WHERE id = ?", Integer.class, id);
    }

    private int pecaEstoque(long id) {
        return jdbcTemplate.queryForObject("SELECT quantidade_estoque FROM peca WHERE id = ?", Integer.class, id);
    }

    private int insumoReservado(long id) {
        return jdbcTemplate.queryForObject("SELECT quantidade_reservada FROM insumo WHERE id = ?", Integer.class, id);
    }

    private List<String> statusReservas() {
        return jdbcTemplate.queryForList("SELECT status FROM reserva_estoque WHERE ordem_servico_id = ?", String.class, ORDEM_ID);
    }

    private io.restassured.specification.RequestSpecification requisicao() {
        return RestAssured.given().contentType("application/json");
    }

    private static final String LOTE_PECA_E_INSUMO = """
            {"itens":[
              {"tipoItem":"PECA","itemId":1,"quantidadeTotal":3},
              {"tipoItem":"INSUMO","itemId":1,"quantidadeTotal":4}
            ]}
            """;

    @Test
    void shouldReservarPecaEInsumoNumaUnicaChamada() {
        criarPeca(1, 10);
        criarInsumo(1, 20);

        requisicao().body(LOTE_PECA_E_INSUMO).when().put(URL + "/itens").then().statusCode(204);

        Assertions.assertEquals(3, pecaReservada(1));
        Assertions.assertEquals(4, insumoReservado(1));
        Assertions.assertEquals(List.of("ATIVA", "ATIVA"), statusReservas());
    }

    @Test
    void shouldSerIdempotenteAoRepetirOMesmoLote() {
        criarPeca(1, 10);
        criarInsumo(1, 20);

        requisicao().body(LOTE_PECA_E_INSUMO).when().put(URL + "/itens").then().statusCode(204);
        requisicao().body(LOTE_PECA_E_INSUMO).when().put(URL + "/itens").then().statusCode(204);

        Assertions.assertEquals(3, pecaReservada(1));
        Assertions.assertEquals(4, insumoReservado(1));
        Assertions.assertEquals(2, statusReservas().size());
    }

    @Test
    void shouldDesfazerLoteInteiroQuandoUmItemNaoTemEstoque() {
        criarPeca(1, 10);
        criarInsumo(1, 2);

        requisicao().body(LOTE_PECA_E_INSUMO).when().put(URL + "/itens").then().statusCode(422);

        Assertions.assertEquals(0, pecaReservada(1), "a peça, reservada antes do insumo, deve ter sofrido rollback");
        Assertions.assertEquals(0, insumoReservado(1));
        Assertions.assertTrue(statusReservas().isEmpty());
    }

    @Test
    void shouldDesfazerLoteInteiroQuandoUmItemNaoExiste() {
        criarPeca(1, 10);

        requisicao().body(LOTE_PECA_E_INSUMO).when().put(URL + "/itens").then().statusCode(404);

        Assertions.assertEquals(0, pecaReservada(1));
        Assertions.assertTrue(statusReservas().isEmpty());
    }

    @Test
    void shouldConfirmarReservasBaixandoOEstoque() {
        criarPeca(1, 10);
        criarInsumo(1, 20);
        requisicao().body(LOTE_PECA_E_INSUMO).when().put(URL + "/itens").then().statusCode(204);

        requisicao().when().post(URL + "/confirmar").then().statusCode(204);

        Assertions.assertEquals(7, pecaEstoque(1));
        Assertions.assertEquals(0, pecaReservada(1));
        Assertions.assertEquals(List.of("CONFIRMADA", "CONFIRMADA"), statusReservas());
    }

    @Test
    void shouldLiberarReservasDevolvendoOSaldo() {
        criarPeca(1, 10);
        criarInsumo(1, 20);
        requisicao().body(LOTE_PECA_E_INSUMO).when().put(URL + "/itens").then().statusCode(204);

        requisicao().when().post(URL + "/liberar").then().statusCode(204);

        Assertions.assertEquals(10, pecaEstoque(1));
        Assertions.assertEquals(0, pecaReservada(1));
        Assertions.assertEquals(List.of("LIBERADA", "LIBERADA"), statusReservas());
    }
}
