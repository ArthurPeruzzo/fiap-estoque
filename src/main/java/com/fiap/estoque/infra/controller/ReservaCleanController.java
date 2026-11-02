package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.dto.ItemReservaDto;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;
import com.fiap.estoque.core.usecase.ConfirmarReservasOrdemDeServicoUseCase;
import com.fiap.estoque.core.usecase.LiberarReservasOrdemDeServicoUseCase;
import com.fiap.estoque.core.usecase.ReservarItensEstoqueUseCase;

import java.util.List;

public class ReservaCleanController {

    private final ItemEstoqueGateway itemEstoqueGateway;
    private final ReservaGateway reservaGateway;
    private final TransacaoGateway transacaoGateway;

    public ReservaCleanController(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway,
                                  TransacaoGateway transacaoGateway) {
        this.itemEstoqueGateway = itemEstoqueGateway;
        this.reservaGateway = reservaGateway;
        this.transacaoGateway = transacaoGateway;
    }

    public void reservar(Long ordemServicoId, List<ItemReservaDto> itens) {
        new ReservarItensEstoqueUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway).reservar(ordemServicoId, itens);
    }

    public void confirmar(Long ordemServicoId) {
        new ConfirmarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway).confirmar(ordemServicoId);
    }

    public void liberar(Long ordemServicoId) {
        new LiberarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway, transacaoGateway).liberar(ordemServicoId);
    }
}
