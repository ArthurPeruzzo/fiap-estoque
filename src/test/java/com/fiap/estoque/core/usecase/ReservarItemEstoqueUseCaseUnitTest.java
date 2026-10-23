package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.*;
import com.fiap.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReservarItemEstoqueUseCaseUnitTest {

    @Mock private ItemEstoqueGateway itemEstoqueGateway;
    @Mock private ReservaGateway reservaGateway;

    private ReservarItemEstoqueUseCase useCase;

    private static final Long ORDEM_ID = 1L;
    private static final Long PECA_ID = 5L;
    private static final Long INSUMO_ID = 30L;

    @BeforeEach
    void setUp() {
        useCase = new ReservarItemEstoqueUseCase(itemEstoqueGateway, reservaGateway);
    }

    private Peca peca(int estoque, int reservada) {
        return Peca.builder().id(PECA_ID).nome("Filtro").descricao("desc").preco(BigDecimal.TEN).quantidadeEstoque(estoque).quantidadeReservada(reservada).build();
    }

    @Test
    void shouldCriarReservaENaoAlterarEstoqueTotal() {
        var peca = peca(10, 0);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenReturn(peca);
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.empty());

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 3);

        assertEquals(10, peca.getEstoqueTotal());
        assertEquals(3, peca.getQuantidadeReservada());
        Mockito.verify(itemEstoqueGateway).atualizar(peca);
        var captor = ArgumentCaptor.forClass(Reserva.class);
        Mockito.verify(reservaGateway).salvar(captor.capture());
        assertEquals(3, captor.getValue().getQuantidade());
        assertEquals(StatusReserva.ATIVA, captor.getValue().getStatus());
    }

    @Test
    void shouldReservarSomenteADiferencaQuandoTotalAumenta() {
        var peca = peca(10, 2);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenReturn(peca);
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.of(
                Reserva.builder().id(7L).ordemServicoId(ORDEM_ID).tipoItem(TipoItemReserva.PECA).itemId(PECA_ID).quantidade(2).status(StatusReserva.ATIVA).build()));

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 5);

        assertEquals(5, peca.getQuantidadeReservada());
    }

    @Test
    void shouldLiberarADiferencaQuandoTotalDiminui() {
        var peca = peca(10, 5);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenReturn(peca);
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.of(
                Reserva.builder().id(7L).ordemServicoId(ORDEM_ID).tipoItem(TipoItemReserva.PECA).itemId(PECA_ID).quantidade(5).status(StatusReserva.ATIVA).build()));

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 0);

        assertEquals(0, peca.getQuantidadeReservada());
        var captor = ArgumentCaptor.forClass(Reserva.class);
        Mockito.verify(reservaGateway).salvar(captor.capture());
        assertEquals(StatusReserva.LIBERADA, captor.getValue().getStatus());
    }

    @Test
    void shouldSerIdempotenteQuandoTotalNaoMuda() {
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenReturn(peca(10, 3));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.of(
                Reserva.builder().id(7L).ordemServicoId(ORDEM_ID).tipoItem(TipoItemReserva.PECA).itemId(PECA_ID).quantidade(3).status(StatusReserva.ATIVA).build()));

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 3);

        Mockito.verify(itemEstoqueGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(reservaGateway, Mockito.never()).salvar(Mockito.any());
    }

    @Test
    void shouldThrowEstoqueInsuficienteSemPersistir() {
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenReturn(peca(10, 9));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.empty());

        assertThrows(EstoqueInsuficienteException.class,
                () -> useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 2));

        Mockito.verify(itemEstoqueGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(reservaGateway, Mockito.never()).salvar(Mockito.any());
    }

    @Test
    void shouldReservarInsumo() {
        var insumo = Insumo.builder().id(INSUMO_ID).nome("Óleo").descricao("desc").preco(BigDecimal.TEN).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(20).quantidadeReservada(0).build();
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.INSUMO, INSUMO_ID)).thenReturn(insumo);
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.INSUMO, INSUMO_ID)).thenReturn(Optional.empty());

        useCase.reservar(ORDEM_ID, TipoItemReserva.INSUMO, INSUMO_ID, 4);

        assertEquals(4, insumo.getQuantidadeReservada());
        Mockito.verify(itemEstoqueGateway).atualizar(insumo);
        Mockito.verify(itemEstoqueGateway, Mockito.never()).buscar(Mockito.eq(TipoItemReserva.PECA), Mockito.any());
    }

    @Test
    void shouldThrowWhenPecaNotFound() {
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenThrow(new PecaNaoEncontradaException());

        assertThrows(PecaNaoEncontradaException.class,
                () -> useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 1));
        Mockito.verifyNoInteractions(reservaGateway);
    }

    @Test
    void shouldThrowWhenInsumoNotFound() {
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.INSUMO, INSUMO_ID)).thenThrow(new InsumoNaoEncontradoException());

        assertThrows(InsumoNaoEncontradoException.class,
                () -> useCase.reservar(ORDEM_ID, TipoItemReserva.INSUMO, INSUMO_ID, 1));
        Mockito.verifyNoInteractions(reservaGateway);
    }
}
