package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.domain.Reserva;
import com.fiap.estoque.core.domain.TipoItemReserva;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.dto.ItemReservaDto;
import com.fiap.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ReservarItensEstoqueUseCaseUnitTest {

    @Mock private ItemEstoqueGateway itemEstoqueGateway;
    @Mock private ReservaGateway reservaGateway;
    private final TransacaoGateway transacaoGateway = Runnable::run;

    private ReservarItensEstoqueUseCase useCase;

    private static final Long ORDEM_ID = 1L;
    private static final Long PECA_ID = 5L;
    private static final Long INSUMO_ID = 30L;

    @BeforeEach
    void setUp() {
        useCase = new ReservarItensEstoqueUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway);
    }

    private Peca peca(int estoque) {
        return Peca.builder().id(PECA_ID).nome("Filtro").descricao("desc").preco(BigDecimal.TEN)
                .quantidadeEstoque(estoque).quantidadeReservada(0).build();
    }

    private Insumo insumo(int estoque) {
        return Insumo.builder().id(INSUMO_ID).nome("Óleo").descricao("desc").preco(BigDecimal.TEN)
                .quantidadeEstoque(estoque).quantidadeReservada(0).unidadeMedida(UnidadeMedida.LITRO).build();
    }

    @Test
    void shouldReservarTodosOsItensDoLote() {
        var peca = peca(10);
        var insumo = insumo(20);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenReturn(peca);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.INSUMO, INSUMO_ID)).thenReturn(insumo);
        Mockito.when(reservaGateway.buscar(Mockito.eq(ORDEM_ID), Mockito.any(), Mockito.any())).thenReturn(Optional.empty());

        useCase.reservar(ORDEM_ID, List.of(
                new ItemReservaDto(TipoItemReserva.PECA, PECA_ID, 3),
                new ItemReservaDto(TipoItemReserva.INSUMO, INSUMO_ID, 4)));

        assertEquals(3, peca.getQuantidadeReservada());
        assertEquals(4, insumo.getQuantidadeReservada());
        Mockito.verify(itemEstoqueGateway).atualizar(peca);
        Mockito.verify(itemEstoqueGateway).atualizar(insumo);
        Mockito.verify(reservaGateway, Mockito.times(2)).salvar(Mockito.any(Reserva.class));
    }

    @Test
    void shouldPropagarFalhaQuandoUmItemNaoTemEstoqueSuficiente() {
        var peca = peca(10);
        var insumo = insumo(2);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, PECA_ID)).thenReturn(peca);
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.INSUMO, INSUMO_ID)).thenReturn(insumo);
        Mockito.when(reservaGateway.buscar(Mockito.eq(ORDEM_ID), Mockito.any(), Mockito.any())).thenReturn(Optional.empty());

        var itens = List.of(
                new ItemReservaDto(TipoItemReserva.PECA, PECA_ID, 3),
                new ItemReservaDto(TipoItemReserva.INSUMO, INSUMO_ID, 4));

        assertThrows(EstoqueInsuficienteException.class, () -> useCase.reservar(ORDEM_ID, itens));

        // O rollback do lote é da transação do controller; aqui só garantimos que o item inválido não foi gravado.
        Mockito.verify(itemEstoqueGateway, Mockito.never()).atualizar(insumo);
    }
}
