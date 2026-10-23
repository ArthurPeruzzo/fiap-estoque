package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.gateway.PecaGateway;
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
@WebMvcTest(controllers = PecaHttpController.class)
class PecaControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PecaGateway pecaGateway;

    private static final String VALID_BODY =
            "{\"nome\":\"Filtro de óleo\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}";

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"preco\":29.90,\"quantidadeEstoque\":10}', descricao, 'A descrição deve ser preenchida'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"quantidadeEstoque\":10}', preco, 'O preço deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90}', quantidadeEstoque, 'A quantidade em estoque deve ser preenchida'"
    })
    void shouldReturn400WhenRequiredFieldIsMissingOnCreate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(pecaGateway);
    }

    @Test
    void shouldReturn400WhenPrecoIsZeroOnCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":0.00,\"quantidadeEstoque\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.preco").value("O preço deve ser maior que zero"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNegativeOnCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidadeEstoque").value("A quantidade em estoque não pode ser negativa"));
    }

    @Test
    void shouldReturn201WhenValidCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(pecaGateway).criar(Mockito.any());
    }

    @Test
    void shouldReturn200WithEmptyPageWhenNoPecas() throws Exception {
        Mockito.when(pecaGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/peca")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void shouldReturn200WithPecasMappedToResponseJson() throws Exception {
        var peca = Peca.builder().id(1L).nome("Filtro").descricao("Desc").preco(new BigDecimal("29.90")).quantidadeEstoque(10).build();
        Mockito.when(pecaGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(peca), 0, 10, 1L, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/peca")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Filtro"))
                .andExpect(jsonPath("$.content[0].preco").value(29.90))
                .andExpect(jsonPath("$.content[0].quantidadeEstoque").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturn204WhenValidUpdate() throws Exception {
        var peca = Peca.builder().id(1L).nome("Filtro").descricao("Desc").preco(new BigDecimal("29.90")).quantidadeEstoque(10).build();
        Mockito.when(pecaGateway.buscarPorId(1L)).thenReturn(Optional.of(peca));

        mockMvc.perform(MockMvcRequestBuilders.put("/peca/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(pecaGateway).atualizar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnUpdate() throws Exception {
        Mockito.when(pecaGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/peca/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"quantidadeEstoque\":10}', preco, 'O preço deve ser preenchido'"
    })
    void shouldReturn400WhenRequiredFieldIsMissingOnUpdate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/peca/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(pecaGateway);
    }

    @Test
    void shouldReturn204WhenDeletarPecaSuccessfully() throws Exception {
        var peca = Peca.builder().id(1L).nome("Filtro").descricao("Desc").preco(new BigDecimal("29.90")).quantidadeEstoque(10).build();
        Mockito.when(pecaGateway.buscarPorId(1L)).thenReturn(Optional.of(peca));

        mockMvc.perform(MockMvcRequestBuilders.delete("/peca/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(pecaGateway).deletar(1L);
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDelete() throws Exception {
        Mockito.when(pecaGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/peca/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }
}
