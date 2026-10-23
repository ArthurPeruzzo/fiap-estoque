package com.fiap.estoque.core.domain;

import com.fiap.estoque.core.exception.EstoqueInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ReservaDeEstoqueUnitTest {

    private final ReservaDeEstoque service = new ReservaDeEstoque();

    private Peca peca(int estoque, int reservada) {
        return Peca.builder().id(1L).nome("P").descricao("d").preco(BigDecimal.TEN)
                .quantidadeEstoque(estoque).quantidadeReservada(reservada).build();
    }

    private Reserva reserva(int quantidade, StatusReserva status) {
        return Reserva.builder().id(1L).ordemServicoId(1L).tipoItem(TipoItemReserva.PECA).itemId(1L)
                .quantidade(quantidade).status(status).build();
    }

    @Test
    void shouldReservarTotalQuandoReservaNova() {
        var item = peca(10, 0);
        var reserva = new Reserva(1L, TipoItemReserva.PECA, 1L);

        assertTrue(service.definirQuantidade(item, reserva, 3));

        assertEquals(3, item.getQuantidadeReservada());
        assertEquals(3, reserva.getQuantidade());
        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
    }

    @Test
    void shouldReservarSomenteADiferencaQuandoTotalAumenta() {
        var item = peca(10, 2);
        var reserva = reserva(2, StatusReserva.ATIVA);

        assertTrue(service.definirQuantidade(item, reserva, 5));

        assertEquals(5, item.getQuantidadeReservada());
    }

    @Test
    void shouldLiberarADiferencaEMarcarLiberadaQuandoTotalZera() {
        var item = peca(10, 5);
        var reserva = reserva(5, StatusReserva.ATIVA);

        assertTrue(service.definirQuantidade(item, reserva, 0));

        assertEquals(0, item.getQuantidadeReservada());
        assertEquals(StatusReserva.LIBERADA, reserva.getStatus());
    }

    @Test
    void shouldSerIdempotenteQuandoTotalNaoMuda() {
        var item = peca(10, 3);
        var reserva = reserva(3, StatusReserva.ATIVA);

        assertFalse(service.definirQuantidade(item, reserva, 3));

        assertEquals(3, item.getQuantidadeReservada());
    }

    @Test
    void shouldReativarReservaLiberada() {
        var item = peca(10, 0);
        var reserva = reserva(0, StatusReserva.LIBERADA);

        assertTrue(service.definirQuantidade(item, reserva, 2));

        assertEquals(2, item.getQuantidadeReservada());
        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
    }

    @Test
    void shouldLancarEstoqueInsuficiente() {
        var item = peca(10, 9);
        var reserva = new Reserva(1L, TipoItemReserva.PECA, 1L);

        assertThrows(EstoqueInsuficienteException.class, () -> service.definirQuantidade(item, reserva, 2));
        assertEquals(9, item.getQuantidadeReservada());
    }
}
