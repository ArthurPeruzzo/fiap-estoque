package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.core.gateway.PecaGateway;
import com.fiap.estoque.resources.NoSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = CatalogoHttpController.class)
class CatalogoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private PecaGateway pecaGateway;
    @MockitoBean private InsumoGateway insumoGateway;

    @Test
    void shouldReturn200ComIdEPrecoDasPecas() throws Exception {
        var peca = Peca.builder().id(5L).nome("Filtro").descricao("desc").preco(new BigDecimal("42.00"))
                .quantidadeEstoque(10).quantidadeReservada(0).build();
        Mockito.when(pecaGateway.listarPorIds(List.of(5L, 6L))).thenReturn(List.of(peca));

        mockMvc.perform(MockMvcRequestBuilders.get("/catalogo/pecas").param("ids", "5,6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].preco").value(42.00))
                .andExpect(jsonPath("$[0].nome").doesNotExist());
    }

    @Test
    void shouldReturn200ComIdEPrecoDosInsumos() throws Exception {
        var insumo = Insumo.builder().id(3L).nome("Óleo").descricao("desc").preco(new BigDecimal("38.00"))
                .quantidadeEstoque(10).quantidadeReservada(0).unidadeMedida(UnidadeMedida.LITRO).build();
        Mockito.when(insumoGateway.listarPorIds(List.of(3L))).thenReturn(List.of(insumo));

        mockMvc.perform(MockMvcRequestBuilders.get("/catalogo/insumos").param("ids", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].preco").value(38.00));
    }

    @Test
    void shouldReturn400QuandoIdsNaoInformados() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/catalogo/pecas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O parâmetro 'ids' deve ser informado"));
    }
}
