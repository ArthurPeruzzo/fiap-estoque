package com.fiap.estoque.core.domain;

import com.fiap.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.estoque.core.exception.QuantidadeEstoqueMenorQueReservadaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PecaUnitTest {

    private static final String NOME = "Filtro de óleo";
    private static final String DESCRICAO = "Filtro de óleo para motor 1.0";
    private static final BigDecimal PRECO = new BigDecimal("29.90");

    @Test
    void shouldCreatePecaWithCorrectFields() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        assertEquals(NOME, peca.getNome());
        assertEquals(DESCRICAO, peca.getDescricao());
        assertEquals(PRECO, peca.getPreco());
        assertEquals(10, peca.getEstoqueTotal());
        assertNull(peca.getId());
    }

    @Test
    void reconstituir_shouldSetIdAndPreserveFields() {
        var peca = Peca.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).quantidadeEstoque(5).build();

        assertEquals(1L, peca.getId());
        assertEquals(NOME, peca.getNome());
        assertEquals(DESCRICAO, peca.getDescricao());
        assertEquals(PRECO, peca.getPreco());
        assertEquals(5, peca.getEstoqueTotal());
    }

    @Test
    void atualizar_shouldChangeAllFields() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        peca.atualizar("Vela NGK", "Vela de ignição", new BigDecimal("18.00"), 25);

        assertEquals("Vela NGK", peca.getNome());
        assertEquals("Vela de ignição", peca.getDescricao());
        assertEquals(new BigDecimal("18.00"), peca.getPreco());
        assertEquals(25, peca.getEstoqueTotal());
    }

    @Test
    void atualizar_shouldAllowZeroQuantidade() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        peca.atualizar(NOME, DESCRICAO, PRECO, 0);

        assertEquals(0, peca.getEstoqueTotal());
    }

    @Test
    void reservar_shouldIncrementReservadaSemAlterarTotal() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        peca.reservar(3);

        assertEquals(10, peca.getEstoqueTotal());
        assertEquals(3, peca.getQuantidadeReservada());
        assertEquals(7, peca.getEstoqueDisponivel());
    }

    @Test
    void reservar_shouldAllowReservarTodoDisponivel() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 5);

        peca.reservar(5);

        assertEquals(0, peca.getEstoqueDisponivel());
    }

    @Test
    void reservar_shouldThrowEstoqueInsuficienteWhenQuantidadeExceedsDisponivel() {
        var peca = Peca.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).quantidadeEstoque(10).quantidadeReservada(8).build();

        assertThrows(EstoqueInsuficienteException.class, () -> peca.reservar(3));
    }

    @Test
    void reservar_shouldThrowEstoqueInsuficienteWhenEstoqueIsZero() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 0);

        assertThrows(EstoqueInsuficienteException.class, () -> peca.reservar(1));
    }

    @Test
    void liberarReserva_shouldDecrementReservada() {
        var peca = Peca.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).quantidadeEstoque(10).quantidadeReservada(5).build();

        peca.liberarReserva(3);

        assertEquals(10, peca.getEstoqueTotal());
        assertEquals(2, peca.getQuantidadeReservada());
    }

    @Test
    void liberarReserva_shouldNotGoBelowZero() {
        var peca = Peca.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).quantidadeEstoque(10).quantidadeReservada(2).build();

        peca.liberarReserva(5);

        assertEquals(0, peca.getQuantidadeReservada());
    }

    @Test
    void confirmarBaixa_shouldDecrementTotalAndReservada() {
        var peca = Peca.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).quantidadeEstoque(10).quantidadeReservada(4).build();

        peca.confirmarBaixa(4);

        assertEquals(6, peca.getEstoqueTotal());
        assertEquals(0, peca.getQuantidadeReservada());
        assertEquals(6, peca.getEstoqueDisponivel());
    }

    @Test
    void atualizar_shouldThrowWhenQuantidadeMenorQueReservada() {
        var peca = Peca.builder().id(1L).nome(NOME).descricao(DESCRICAO).preco(PRECO).quantidadeEstoque(10).quantidadeReservada(6).build();

        assertThrows(QuantidadeEstoqueMenorQueReservadaException.class,
                () -> peca.atualizar(NOME, DESCRICAO, PRECO, 5));
    }

    @Test
    void novo_shouldStartWithReservadaZero() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 7);

        assertEquals(0, peca.getQuantidadeReservada());
    }
}
