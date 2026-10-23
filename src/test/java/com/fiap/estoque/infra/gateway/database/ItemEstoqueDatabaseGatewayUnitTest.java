package com.fiap.estoque.infra.gateway.database;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.domain.TipoItemReserva;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.core.gateway.PecaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ItemEstoqueDatabaseGatewayUnitTest {

    @Mock private PecaGateway pecaGateway;
    @Mock private InsumoGateway insumoGateway;

    @InjectMocks
    private ItemEstoqueDatabaseGateway gateway;

    private final Peca peca = Peca.builder().id(5L).nome("P").descricao("d").preco(BigDecimal.ONE).quantidadeEstoque(10).build();
    private final Insumo insumo = Insumo.builder().id(30L).nome("I").descricao("d").preco(BigDecimal.ONE)
            .unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(10).build();

    @Test
    void buscar_shouldDelegarParaPecaGateway() {
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(peca));

        assertSame(peca, gateway.buscar(TipoItemReserva.PECA, 5L));
        Mockito.verifyNoInteractions(insumoGateway);
    }

    @Test
    void buscar_shouldDelegarParaInsumoGateway() {
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumo));

        assertSame(insumo, gateway.buscar(TipoItemReserva.INSUMO, 30L));
        Mockito.verifyNoInteractions(pecaGateway);
    }

    @Test
    void buscar_shouldThrowWhenPecaNotFound() {
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.empty());

        assertThrows(PecaNaoEncontradaException.class, () -> gateway.buscar(TipoItemReserva.PECA, 5L));
    }

    @Test
    void buscar_shouldThrowWhenInsumoNotFound() {
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoException.class, () -> gateway.buscar(TipoItemReserva.INSUMO, 30L));
    }

    @Test
    void atualizar_shouldRotearPorTipoDoItem() {
        gateway.atualizar(peca);
        gateway.atualizar(insumo);

        Mockito.verify(pecaGateway).atualizar(peca);
        Mockito.verify(insumoGateway).atualizar(insumo);
    }
}
