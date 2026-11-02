package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.domain.TipoItemReserva;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;
import com.fiap.estoque.resources.NoSecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = ReservaHttpController.class)
class ReservaControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ItemEstoqueGateway itemEstoqueGateway;
    @MockitoBean private ReservaGateway reservaGateway;
    @MockitoBean private TransacaoGateway transacaoGateway;

    @BeforeEach
    void executarTransacaoDiretamente() {
        Mockito.doAnswer(inv -> {
            inv.<Runnable>getArgument(0).run();
            return null;
        }).when(transacaoGateway).executar(Mockito.any());
    }

    private static final String URL = "/reserva/ordem-servico/7";

    @Test
    void shouldReturn204WhenValidReserva() throws Exception {
        var peca = Peca.builder().id(5L).nome("Filtro").descricao("desc").preco(BigDecimal.TEN)
                .quantidadeEstoque(10).quantidadeReservada(0).build();
        Mockito.when(itemEstoqueGateway.buscar(TipoItemReserva.PECA, 5L)).thenReturn(peca);
        Mockito.when(reservaGateway.buscar(7L, TipoItemReserva.PECA, 5L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[{\"tipoItem\":\"PECA\",\"itemId\":5,\"quantidadeTotal\":3}]}"))
                .andExpect(status().isNoContent());

        Mockito.verify(itemEstoqueGateway).atualizar(peca);
        Mockito.verify(reservaGateway).salvar(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource({
            "'{}', itens, 'Os itens devem ser informados'",
            "'{\"itens\":[]}', itens, 'Os itens devem ser informados'"
    })
    void shouldReturn400WhenItensMissing(String body, String field, String message) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(message));

        Mockito.verifyNoInteractions(itemEstoqueGateway, reservaGateway);
    }

    @ParameterizedTest
    @CsvSource({
            "'{\"itens\":[{\"itemId\":5,\"quantidadeTotal\":3}]}'",
            "'{\"itens\":[{\"tipoItem\":\"PECA\",\"quantidadeTotal\":3}]}'",
            "'{\"itens\":[{\"tipoItem\":\"PECA\",\"itemId\":5}]}'",
            "'{\"itens\":[{\"tipoItem\":\"PECA\",\"itemId\":5,\"quantidadeTotal\":-1}]}'"
    })
    void shouldReturn400WhenItemInvalid(String body) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemEstoqueGateway, reservaGateway);
    }

    @Test
    void shouldReturn204WhenConfirmar() throws Exception {
        Mockito.when(reservaGateway.listarAtivasPorOrdemDeServico(7L)).thenReturn(java.util.List.of());

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/confirmar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn204WhenLiberar() throws Exception {
        Mockito.when(reservaGateway.listarAtivasPorOrdemDeServico(7L)).thenReturn(java.util.List.of());

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/liberar"))
                .andExpect(status().isNoContent());
    }
}
