package com.fiap.estoque.infra.controller.presenter;

import com.fiap.estoque.core.dto.ItemCatalogoDto;
import com.fiap.estoque.core.usecase.ConsultarCatalogoOutputPort;
import com.fiap.estoque.infra.controller.json.ItemCatalogoResponseJson;

import java.util.List;

public class ConsultarCatalogoPresenter implements ConsultarCatalogoOutputPort {

    private List<ItemCatalogoResponseJson> viewModel;

    @Override
    public void apresentar(List<ItemCatalogoDto> itens) {
        this.viewModel = itens.stream()
                .map(item -> new ItemCatalogoResponseJson(item.id(), item.preco()))
                .toList();
    }

    public List<ItemCatalogoResponseJson> getViewModel() {
        return viewModel;
    }
}
