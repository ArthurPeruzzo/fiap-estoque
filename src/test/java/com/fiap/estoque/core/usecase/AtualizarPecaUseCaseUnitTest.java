package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.dto.AtualizarPecaDto;
import com.fiap.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.estoque.core.gateway.PecaGateway;
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
class AtualizarPecaUseCaseUnitTest {

    @InjectMocks
    private AtualizarPecaUseCase atualizarPecaUseCase;

    @Mock
    private PecaGateway pecaGateway;

    @Test
    void shouldUpdatePecaWithNewValues() {
        var peca = Peca.builder().id(1L).nome("Filtro velho").descricao("Desc antiga").preco(new BigDecimal("20.00")).quantidadeEstoque(5).build();
        Mockito.when(pecaGateway.buscarPorId(1L)).thenReturn(Optional.of(peca));
        var dto = new AtualizarPecaDto(1L, "Filtro novo", "Desc nova", new BigDecimal("35.00"), 10);
        var captor = ArgumentCaptor.forClass(Peca.class);

        atualizarPecaUseCase.atualizar(dto);

        Mockito.verify(pecaGateway).atualizar(captor.capture());
        var atualizada = captor.getValue();
        assertEquals("Filtro novo", atualizada.getNome());
        assertEquals("Desc nova", atualizada.getDescricao());
        assertEquals(new BigDecimal("35.00"), atualizada.getPreco());
        assertEquals(10, atualizada.getEstoqueTotal());
        assertEquals(1L, atualizada.getId());
    }

    @Test
    void shouldThrowPecaNaoEncontradaExceptionWhenPecaDoesNotExist() {
        Mockito.when(pecaGateway.buscarPorId(99L)).thenReturn(Optional.empty());
        var dto = new AtualizarPecaDto(99L, "Filtro", "Desc", new BigDecimal("10.00"), 5);

        assertThrows(PecaNaoEncontradaException.class, () -> atualizarPecaUseCase.atualizar(dto));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
