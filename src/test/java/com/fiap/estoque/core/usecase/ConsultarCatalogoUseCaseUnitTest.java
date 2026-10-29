package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.dto.ItemCatalogoDto;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.core.gateway.PecaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ConsultarCatalogoUseCaseUnitTest {

    @Mock private PecaGateway pecaGateway;
    @Mock private InsumoGateway insumoGateway;

    private final List<ItemCatalogoDto> apresentados = new ArrayList<>();
    private final ConsultarCatalogoOutputPort outputPort = apresentados::addAll;

    @Test
    void consultarPecas_shouldApresentarIdEPreco() {
        var peca = Peca.builder().id(5L).nome("Filtro").descricao("desc").preco(new BigDecimal("42.00"))
                .quantidadeEstoque(10).quantidadeReservada(0).build();
        Mockito.when(pecaGateway.listarPorIds(List.of(5L, 6L))).thenReturn(List.of(peca));

        new ConsultarCatalogoPecasUseCase(pecaGateway, outputPort).consultar(List.of(5L, 6L));

        assertEquals(List.of(new ItemCatalogoDto(5L, new BigDecimal("42.00"))), apresentados);
    }

    @Test
    void consultarInsumos_shouldApresentarIdEPreco() {
        var insumo = Insumo.builder().id(3L).nome("Óleo").descricao("desc").preco(new BigDecimal("38.00"))
                .quantidadeEstoque(10).quantidadeReservada(0).unidadeMedida(UnidadeMedida.LITRO).build();
        Mockito.when(insumoGateway.listarPorIds(List.of(3L))).thenReturn(List.of(insumo));

        new ConsultarCatalogoInsumosUseCase(insumoGateway, outputPort).consultar(List.of(3L));

        assertEquals(List.of(new ItemCatalogoDto(3L, new BigDecimal("38.00"))), apresentados);
    }
}
