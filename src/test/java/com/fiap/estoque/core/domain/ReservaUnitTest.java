package com.fiap.estoque.core.domain;

import com.fiap.estoque.core.exception.ReservaJaConfirmadaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservaUnitTest {

    @Test
    void novaReserva_shouldStartAtivaComQuantidadeZero() {
        var reserva = new Reserva(1L, TipoItemReserva.PECA, 5L);

        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
        assertEquals(0, reserva.getQuantidade());
    }

    @Test
    void definirQuantidade_shouldLiberarWhenZero() {
        var reserva = new Reserva(1L, TipoItemReserva.PECA, 5L);
        reserva.definirQuantidade(3);

        reserva.definirQuantidade(0);

        assertEquals(StatusReserva.LIBERADA, reserva.getStatus());
        assertEquals(0, reserva.quantidadeAtiva());
    }

    @Test
    void definirQuantidade_shouldReativarReservaLiberada() {
        var reserva = Reserva.builder().id(9L).ordemServicoId(1L).tipoItem(TipoItemReserva.INSUMO).itemId(5L).quantidade(2).status(StatusReserva.LIBERADA).build();

        reserva.definirQuantidade(4);

        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
        assertEquals(4, reserva.quantidadeAtiva());
    }

    @Test
    void definirQuantidade_shouldThrowWhenConfirmada() {
        var reserva = Reserva.builder().id(9L).ordemServicoId(1L).tipoItem(TipoItemReserva.PECA).itemId(5L).quantidade(2).status(StatusReserva.CONFIRMADA).build();

        assertThrows(ReservaJaConfirmadaException.class, () -> reserva.definirQuantidade(3));
    }

    @Test
    void quantidadeAtiva_shouldBeZeroWhenConfirmada() {
        var reserva = Reserva.builder().id(9L).ordemServicoId(1L).tipoItem(TipoItemReserva.PECA).itemId(5L).quantidade(2).status(StatusReserva.ATIVA).build();

        reserva.confirmar();

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        assertEquals(0, reserva.quantidadeAtiva());
    }
}
