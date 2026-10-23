package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.estoque.core.gateway.InsumoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DeletarInsumoUseCaseUnitTest {

    @InjectMocks
    private DeletarInsumoUseCase deletarInsumoUseCase;

    @Mock
    private InsumoGateway insumoGateway;

    @Test
    void shouldCallDeleteWhenInsumoExists() {
        var insumo = Insumo.builder().id(1L).nome("Óleo").descricao("Desc").preco(new BigDecimal("30.00")).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(5).build();
        Mockito.when(insumoGateway.buscarPorId(1L)).thenReturn(Optional.of(insumo));

        deletarInsumoUseCase.deletar(1L);

        Mockito.verify(insumoGateway).deletar(1L);
    }

    @Test
    void shouldThrowInsumoNaoEncontradoExceptionWhenInsumoDoesNotExist() {
        Mockito.when(insumoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoException.class, () -> deletarInsumoUseCase.deletar(99L));

        Mockito.verify(insumoGateway, Mockito.never()).deletar(Mockito.anyLong());
    }
}
