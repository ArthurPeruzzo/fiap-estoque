package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.core.gateway.PecaGateway;
import com.fiap.estoque.core.usecase.ConsultarCatalogoInsumosUseCase;
import com.fiap.estoque.core.usecase.ConsultarCatalogoPecasUseCase;
import com.fiap.estoque.infra.controller.json.ItemCatalogoResponseJson;
import com.fiap.estoque.infra.controller.presenter.ConsultarCatalogoPresenter;

import java.util.List;

public class CatalogoCleanController {

    private final PecaGateway pecaGateway;
    private final InsumoGateway insumoGateway;

    public CatalogoCleanController(PecaGateway pecaGateway, InsumoGateway insumoGateway) {
        this.pecaGateway = pecaGateway;
        this.insumoGateway = insumoGateway;
    }

    public List<ItemCatalogoResponseJson> consultarPecas(List<Long> ids) {
        var presenter = new ConsultarCatalogoPresenter();
        new ConsultarCatalogoPecasUseCase(pecaGateway, presenter).consultar(ids);
        return presenter.getViewModel();
    }

    public List<ItemCatalogoResponseJson> consultarInsumos(List<Long> ids) {
        var presenter = new ConsultarCatalogoPresenter();
        new ConsultarCatalogoInsumosUseCase(insumoGateway, presenter).consultar(ids);
        return presenter.getViewModel();
    }
}
