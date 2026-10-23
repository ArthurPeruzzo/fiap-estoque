package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.dto.CriarPecaDto;
import com.fiap.estoque.core.gateway.PecaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CriarPecaUseCaseUnitTest {

    @InjectMocks
    private CriarPecaUseCase criarPecaUseCase;

    @Mock
    private PecaGateway pecaGateway;

    @Test
    void shouldDelegateToGatewayWithCorrectFields() {
        var dto = new CriarPecaDto("Filtro de óleo", "Filtro 1.0", new BigDecimal("29.90"), 10);
        var captor = ArgumentCaptor.forClass(Peca.class);

        criarPecaUseCase.criar(dto);

        Mockito.verify(pecaGateway).criar(captor.capture());
        var peca = captor.getValue();
        assertEquals("Filtro de óleo", peca.getNome());
        assertEquals("Filtro 1.0", peca.getDescricao());
        assertEquals(new BigDecimal("29.90"), peca.getPreco());
        assertEquals(10, peca.getEstoqueTotal());
    }

    @Test
    void shouldPropagateExceptionFromGateway() {
        var dto = new CriarPecaDto("Filtro de óleo", "Filtro 1.0", new BigDecimal("29.90"), 10);
        Mockito.doThrow(new RuntimeException("erro no banco")).when(pecaGateway).criar(Mockito.any());

        assertThrows(RuntimeException.class, () -> criarPecaUseCase.criar(dto));
    }
}
