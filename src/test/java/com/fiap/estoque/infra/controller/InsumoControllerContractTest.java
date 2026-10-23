package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.resources.NoSecurityConfiguration;
import com.fiap.estoque.shared.page.Pagina;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = InsumoHttpController.class)
class InsumoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsumoGateway insumoGateway;

    private static final String VALID_BODY =
            "{\"nome\":\"Óleo de motor\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}";

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Óleo\",\"preco\":45.90,\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}', descricao, 'A descrição deve ser preenchida'",
            "'{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}', preco, 'O preço deve ser preenchido'",
            "'{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"unidadeMedida\":\"LITRO\"}', quantidadeEstoque, 'A quantidade em estoque deve ser preenchida'",
            "'{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":10}', unidadeMedida, 'A unidade de medida deve ser preenchida'"
    })
    void shouldReturn400WhenRequiredFieldIsMissingOnCreate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(insumoGateway);
    }

    @Test
    void shouldReturn201WhenValidCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(insumoGateway).criar(Mockito.any());
    }

    @Test
    void shouldReturn200WithEmptyPageWhenNoInsumos() throws Exception {
        Mockito.when(insumoGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/insumo")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void shouldReturn200WithInsumosMappedToResponseJson() throws Exception {
        var insumo = Insumo.builder().id(1L).nome("Óleo").descricao("5W30").preco(new BigDecimal("45.90")).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(10).build();
        Mockito.when(insumoGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(insumo), 0, 10, 1L, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/insumo")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Óleo"))
                .andExpect(jsonPath("$.content[0].unidadeMedida").value("LITRO"))
                .andExpect(jsonPath("$.content[0].quantidadeEstoque").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturn204WhenValidUpdate() throws Exception {
        var insumo = Insumo.builder().id(1L).nome("Óleo").descricao("5W30").preco(new BigDecimal("45.90")).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(10).build();
        Mockito.when(insumoGateway.buscarPorId(1L)).thenReturn(Optional.of(insumo));

        mockMvc.perform(MockMvcRequestBuilders.put("/insumo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(insumoGateway).atualizar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnUpdate() throws Exception {
        Mockito.when(insumoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/insumo/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn400WhenUnidadeMedidaIsMissingOnUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/insumo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Óleo\",\"descricao\":\"5W30\",\"preco\":45.90,\"quantidadeEstoque\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.unidadeMedida").value("A unidade de medida deve ser preenchida"));

        Mockito.verifyNoInteractions(insumoGateway);
    }

    @Test
    void shouldReturn204WhenDeletarInsumoSuccessfully() throws Exception {
        var insumo = Insumo.builder().id(1L).nome("Óleo").descricao("5W30").preco(new BigDecimal("45.90")).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(10).build();
        Mockito.when(insumoGateway.buscarPorId(1L)).thenReturn(Optional.of(insumo));

        mockMvc.perform(MockMvcRequestBuilders.delete("/insumo/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(insumoGateway).deletar(1L);
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDelete() throws Exception {
        Mockito.when(insumoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/insumo/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }
}
