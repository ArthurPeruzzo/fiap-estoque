package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.dto.CriarInsumoDto;
import com.fiap.estoque.core.gateway.InsumoGateway;
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
class CriarInsumoUseCaseUnitTest {

    @InjectMocks
    private CriarInsumoUseCase criarInsumoUseCase;

    @Mock
    private InsumoGateway insumoGateway;

    @Test
    void shouldDelegateToGatewayWithCorrectFields() {
        var dto = new CriarInsumoDto("Óleo de motor", "Óleo 5W30", new BigDecimal("45.90"), 10, UnidadeMedida.LITRO);
        var captor = ArgumentCaptor.forClass(Insumo.class);

        criarInsumoUseCase.criar(dto);

        Mockito.verify(insumoGateway).criar(captor.capture());
        var insumo = captor.getValue();
        assertEquals("Óleo de motor", insumo.getNome());
        assertEquals("Óleo 5W30", insumo.getDescricao());
        assertEquals(new BigDecimal("45.90"), insumo.getPreco());
        assertEquals(10, insumo.getEstoqueTotal());
        assertEquals(UnidadeMedida.LITRO, insumo.getUnidadeMedida());
    }

    @Test
    void shouldPropagateExceptionFromGateway() {
        var dto = new CriarInsumoDto("Óleo de motor", "Óleo 5W30", new BigDecimal("45.90"), 10, UnidadeMedida.LITRO);
        Mockito.doThrow(new RuntimeException("erro no banco")).when(insumoGateway).criar(Mockito.any());

        assertThrows(RuntimeException.class, () -> criarInsumoUseCase.criar(dto));
    }
}
