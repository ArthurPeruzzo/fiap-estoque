package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.dto.ListarPecasDto;
import com.fiap.estoque.core.gateway.PecaGateway;
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
class ListarPecasUseCaseUnitTest {

    @Mock
    private PecaGateway pecaGateway;

    @Mock
    private ListarPecasOutputPort outputPort;

    private ListarPecasUseCase listarPecasUseCase;

    @BeforeEach
    void setUp() {
        listarPecasUseCase = new ListarPecasUseCase(pecaGateway, outputPort);
    }

    @Test
    void shouldDelegateToGatewayWithCorrectPageAndSize() {
        var pagina = new Pagina<Peca>(List.of(), 0, 10, 0L, 0);
        Mockito.when(pecaGateway.listar(0, 10)).thenReturn(pagina);

        listarPecasUseCase.listar(new ListarPecasDto(0, 10));

        Mockito.verify(pecaGateway).listar(0, 10);
        Mockito.verify(outputPort).apresentar(pagina);
    }

    @Test
    void shouldCallOutputPortWithPaginaFromGateway() {
        var peca = Peca.builder().id(1L).nome("Filtro").descricao("Desc").preco(new BigDecimal("20.00")).quantidadeEstoque(5).build();
        var pagina = new Pagina<>(List.of(peca), 0, 10, 1L, 1);
        Mockito.when(pecaGateway.listar(0, 10)).thenReturn(pagina);

        listarPecasUseCase.listar(new ListarPecasDto(0, 10));

        Mockito.verify(outputPort).apresentar(pagina);
    }
}
