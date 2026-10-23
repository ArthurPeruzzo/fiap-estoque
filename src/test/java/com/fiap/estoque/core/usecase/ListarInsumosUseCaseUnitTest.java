package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.dto.ListarInsumosDto;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.shared.page.Pagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListarInsumosUseCaseUnitTest {

    @Mock
    private InsumoGateway insumoGateway;

    @Mock
    private ListarInsumosOutputPort outputPort;

    private ListarInsumosUseCase listarInsumosUseCase;

    @BeforeEach
    void setUp() {
        listarInsumosUseCase = new ListarInsumosUseCase(insumoGateway, outputPort);
    }

    @Test
    void shouldDelegateToGatewayWithCorrectPageAndSize() {
        var pagina = new Pagina<Insumo>(List.of(), 0, 10, 0L, 0);
        Mockito.when(insumoGateway.listar(0, 10)).thenReturn(pagina);

        listarInsumosUseCase.listar(new ListarInsumosDto(0, 10));

        Mockito.verify(insumoGateway).listar(0, 10);
        Mockito.verify(outputPort).apresentar(pagina);
    }

    @Test
    void shouldCallOutputPortWithPaginaFromGateway() {
        var insumo = Insumo.builder().id(1L).nome("Óleo").descricao("Desc").preco(new BigDecimal("30.00")).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(5).build();
        var pagina = new Pagina<>(List.of(insumo), 0, 10, 1L, 1);
        Mockito.when(insumoGateway.listar(0, 10)).thenReturn(pagina);

        listarInsumosUseCase.listar(new ListarInsumosDto(0, 10));

        Mockito.verify(outputPort).apresentar(pagina);
    }
}
