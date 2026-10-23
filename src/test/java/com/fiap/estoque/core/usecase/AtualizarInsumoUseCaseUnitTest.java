package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.dto.AtualizarInsumoDto;
import com.fiap.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.estoque.core.gateway.InsumoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AtualizarInsumoUseCaseUnitTest {

    @InjectMocks
    private AtualizarInsumoUseCase atualizarInsumoUseCase;

    @Mock
    private InsumoGateway insumoGateway;

    @Test
    void shouldUpdateInsumoWithNewValues() {
        var insumo = Insumo.builder().id(1L).nome("Óleo velho").descricao("Desc antiga").preco(new BigDecimal("30.00")).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(5).build();
        Mockito.when(insumoGateway.buscarPorId(1L)).thenReturn(Optional.of(insumo));
        var dto = new AtualizarInsumoDto(1L, "Óleo novo", "Desc nova", new BigDecimal("50.00"), UnidadeMedida.ML, 20);
        var captor = ArgumentCaptor.forClass(Insumo.class);

        atualizarInsumoUseCase.atualizar(dto);

        Mockito.verify(insumoGateway).atualizar(captor.capture());
        var atualizado = captor.getValue();
        assertEquals("Óleo novo", atualizado.getNome());
        assertEquals("Desc nova", atualizado.getDescricao());
        assertEquals(new BigDecimal("50.00"), atualizado.getPreco());
        assertEquals(UnidadeMedida.ML, atualizado.getUnidadeMedida());
        assertEquals(20, atualizado.getEstoqueTotal());
        assertEquals(1L, atualizado.getId());
    }

    @Test
    void shouldThrowInsumoNaoEncontradoExceptionWhenInsumoDoesNotExist() {
        Mockito.when(insumoGateway.buscarPorId(99L)).thenReturn(Optional.empty());
        var dto = new AtualizarInsumoDto(99L, "Óleo", "Desc", new BigDecimal("10.00"), UnidadeMedida.LITRO, 5);

        assertThrows(InsumoNaoEncontradoException.class, () -> atualizarInsumoUseCase.atualizar(dto));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
