package com.fiap.estoque.core.domain;

import com.fiap.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.estoque.core.exception.QuantidadeEstoqueMenorQueReservadaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InsumoUnitTest {

    private static final String NOME = "Óleo de motor";
    private static final String DESCRICAO = "Óleo lubrificante 5W30";
    private static final BigDecimal PRECO = new BigDecimal("45.90");
    private static final UnidadeMedida UNIDADE = UnidadeMedida.LITRO;

    @Test
    void shouldCreateInsumoWithCorrectFields() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 10);

        assertEquals(NOME, insumo.getNome());
        assertEquals(DESCRICAO, insumo.getDescricao());
        assertEquals(PRECO, insumo.getPreco());
        assertEquals(UNIDADE, insumo.getUnidadeMedida());
        assertEquals(10, insumo.getEstoqueTotal());
        assertNull(insumo.getId());
    }

    @Test
    void reconstituir_shouldSetIdAndPreserveFields() {
        var insumo = Insumo.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).unidadeMedida(UNIDADE).quantidadeEstoque(5).build();

        assertEquals(1L, insumo.getId());
        assertEquals(NOME, insumo.getNome());
        assertEquals(DESCRICAO, insumo.getDescricao());
        assertEquals(PRECO, insumo.getPreco());
        assertEquals(UNIDADE, insumo.getUnidadeMedida());
        assertEquals(5, insumo.getEstoqueTotal());
    }

    @Test
    void reconstituir_shouldWorkWithDifferentUnidadeMedida() {
        var insumo = Insumo.builder().id(2L).nome(NOME).descricao(DESCRICAO).preco(PRECO).unidadeMedida(UnidadeMedida.ML).quantidadeEstoque(500).build();

        assertEquals(UnidadeMedida.ML, insumo.getUnidadeMedida());
        assertEquals(500, insumo.getEstoqueTotal());
    }

    @Test
    void atualizar_shouldChangeAllFields() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 10);

        insumo.atualizar("Fluido de freio", "DOT 4", new BigDecimal("25.00"), UnidadeMedida.ML, 500);

        assertEquals("Fluido de freio", insumo.getNome());
        assertEquals("DOT 4", insumo.getDescricao());
        assertEquals(new BigDecimal("25.00"), insumo.getPreco());
        assertEquals(UnidadeMedida.ML, insumo.getUnidadeMedida());
        assertEquals(500, insumo.getEstoqueTotal());
    }

    @Test
    void atualizar_shouldAllowZeroQuantidade() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 10);

        insumo.atualizar(NOME, DESCRICAO, PRECO, UNIDADE, 0);

        assertEquals(0, insumo.getEstoqueTotal());
    }

    @Test
    void reservar_shouldIncrementReservadaSemAlterarTotal() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 10);

        insumo.reservar(3);

        assertEquals(10, insumo.getEstoqueTotal());
        assertEquals(3, insumo.getQuantidadeReservada());
        assertEquals(7, insumo.getEstoqueDisponivel());
    }

    @Test
    void reservar_shouldAllowReservarTodoDisponivel() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 5);

        insumo.reservar(5);

        assertEquals(0, insumo.getEstoqueDisponivel());
    }

    @Test
    void reservar_shouldThrowEstoqueInsuficienteWhenQuantidadeExceedsDisponivel() {
        var insumo = Insumo.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).unidadeMedida(UNIDADE).quantidadeEstoque(10).quantidadeReservada(8).build();

        assertThrows(EstoqueInsuficienteException.class, () -> insumo.reservar(3));
    }

    @Test
    void reservar_shouldThrowEstoqueInsuficienteWhenEstoqueIsZero() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 0);

        assertThrows(EstoqueInsuficienteException.class, () -> insumo.reservar(1));
    }

    @Test
    void liberarReserva_shouldDecrementReservada() {
        var insumo = Insumo.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).unidadeMedida(UNIDADE).quantidadeEstoque(10).quantidadeReservada(5).build();

        insumo.liberarReserva(3);

        assertEquals(10, insumo.getEstoqueTotal());
        assertEquals(2, insumo.getQuantidadeReservada());
    }

    @Test
    void liberarReserva_shouldNotGoBelowZero() {
        var insumo = Insumo.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).unidadeMedida(UNIDADE).quantidadeEstoque(10).quantidadeReservada(2).build();

        insumo.liberarReserva(5);

        assertEquals(0, insumo.getQuantidadeReservada());
    }

    @Test
    void confirmarBaixa_shouldDecrementTotalAndReservada() {
        var insumo = Insumo.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).unidadeMedida(UNIDADE).quantidadeEstoque(10).quantidadeReservada(4).build();

        insumo.confirmarBaixa(4);

        assertEquals(6, insumo.getEstoqueTotal());
        assertEquals(0, insumo.getQuantidadeReservada());
        assertEquals(6, insumo.getEstoqueDisponivel());
    }

    @Test
    void atualizar_shouldThrowWhenQuantidadeMenorQueReservada() {
        var insumo = Insumo.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).unidadeMedida(UNIDADE).quantidadeEstoque(10).quantidadeReservada(6).build();

        assertThrows(QuantidadeEstoqueMenorQueReservadaException.class,
                () -> insumo.atualizar(NOME, DESCRICAO, PRECO, UNIDADE, 5));
    }

    @Test
    void novo_shouldStartWithReservadaZero() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 7);

        assertEquals(0, insumo.getQuantidadeReservada());
    }
}
