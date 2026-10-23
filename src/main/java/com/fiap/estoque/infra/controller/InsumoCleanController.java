package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.dto.AtualizarInsumoDto;
import com.fiap.estoque.core.dto.CriarInsumoDto;
import com.fiap.estoque.core.dto.ListarInsumosDto;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.core.usecase.AtualizarInsumoUseCase;
import com.fiap.estoque.core.usecase.CriarInsumoUseCase;
import com.fiap.estoque.core.usecase.DeletarInsumoUseCase;
import com.fiap.estoque.core.usecase.ListarInsumosUseCase;
import com.fiap.estoque.infra.controller.json.InsumoResponseJson;
import com.fiap.estoque.infra.controller.presenter.ListarInsumosPresenter;
import com.fiap.estoque.shared.page.Pagina;

public class InsumoCleanController {

    private final InsumoGateway insumoGateway;

    public InsumoCleanController(InsumoGateway insumoGateway) {
        this.insumoGateway = insumoGateway;
    }

    public void criar(CriarInsumoDto dto) {
        new CriarInsumoUseCase(insumoGateway).criar(dto);
    }

    public Pagina<InsumoResponseJson> listar(ListarInsumosDto dto) {
        var presenter = new ListarInsumosPresenter();
        new ListarInsumosUseCase(insumoGateway, presenter).listar(dto);
        return presenter.getViewModel();
    }

    public void atualizar(AtualizarInsumoDto dto) {
        new AtualizarInsumoUseCase(insumoGateway).atualizar(dto);
    }

    public void deletar(Long id) {
        new DeletarInsumoUseCase(insumoGateway).deletar(id);
    }
}
