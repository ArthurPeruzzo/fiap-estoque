package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.*;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ConfirmarELiberarReservasUseCaseUnitTest {

    @Mock private ItemEstoqueGateway itemEstoqueGateway;
    @Mock private ReservaGateway reservaGateway;
    private final TransacaoGateway transacaoGateway = Runnable::run;

    private static final Long ORDEM_ID = 1L;

    private Peca peca;
    private Insumo insumo;
    private Reserva reservaPeca;
    private Reserva reservaInsumo;

    private void stubReservasAtivas() {
        peca = Peca.builder().id(5L).nome("Filtro").descricao("desc").preco(BigDecimal.TEN).quantidadeEstoque(10).quantidadeReservada(3).build();
        insumo = Insumo.builder().id(30L).nome("Óleo").descricao("desc").preco(BigDecimal.TEN).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(20).quantidadeReservada(4).build();
        reservaPeca = Reserva.builder().id(1L).ordemServicoId(ORDEM_ID).tipoItem(TipoItemReserva.PECA).itemId(5L).quantidade(3).status(StatusReserva.ATIVA).build();
        reservaInsumo = Reserva.builder().id(2L).ordemServicoId(ORDEM_ID).tipoItem(TipoItemReserva.INSUMO).itemId(30L).quantidade(4).status(StatusReserva.ATIVA).build();
        Mockito.when(reservaGateway.listarAtivasPorOrdemDeServico(ORDEM_ID)).thenReturn(List.of(reservaPeca, reservaInsumo));
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, 5L)).thenReturn(peca);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.INSUMO, 30L)).thenReturn(insumo);
    }

    @Test
    void confirmar_shouldBaixarEstoqueEConfirmarReservas() {
        stubReservasAtivas();

        new ConfirmarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway).confirmar(ORDEM_ID);

        assertEquals(7, peca.getEstoqueTotal());
        assertEquals(0, peca.getQuantidadeReservada());
        assertEquals(16, insumo.getEstoqueTotal());
        assertEquals(0, insumo.getQuantidadeReservada());
        assertEquals(StatusReserva.CONFIRMADA, reservaPeca.getStatus());
        assertEquals(StatusReserva.CONFIRMADA, reservaInsumo.getStatus());
        Mockito.verify(itemEstoqueGateway).atualizar(peca);
        Mockito.verify(itemEstoqueGateway).atualizar(insumo);
        Mockito.verify(reservaGateway, Mockito.times(2)).salvar(Mockito.any());
    }

    @Test
    void liberar_shouldDevolverAoDisponivelSemAlterarTotal() {
        stubReservasAtivas();

        new LiberarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway).liberar(ORDEM_ID);

        assertEquals(10, peca.getEstoqueTotal());
        assertEquals(0, peca.getQuantidadeReservada());
        assertEquals(20, insumo.getEstoqueTotal());
        assertEquals(0, insumo.getQuantidadeReservada());
        assertEquals(StatusReserva.LIBERADA, reservaPeca.getStatus());
        assertEquals(StatusReserva.LIBERADA, reservaInsumo.getStatus());
        Mockito.verify(reservaGateway, Mockito.times(2)).salvar(Mockito.any());
    }

    @Test
    void shouldNaoFazerNadaQuandoNaoHaReservasAtivas() {
        Mockito.when(reservaGateway.listarAtivasPorOrdemDeServico(ORDEM_ID)).thenReturn(List.of());

        new ConfirmarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway).confirmar(ORDEM_ID);
        new LiberarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway).liberar(ORDEM_ID);

        Mockito.verifyNoInteractions(itemEstoqueGateway);
        Mockito.verify(reservaGateway, Mockito.never()).salvar(Mockito.any());
    }
}
