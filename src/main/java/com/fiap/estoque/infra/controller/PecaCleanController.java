package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.dto.AtualizarPecaDto;
import com.fiap.estoque.core.dto.CriarPecaDto;
import com.fiap.estoque.core.dto.ListarPecasDto;
import com.fiap.estoque.core.gateway.PecaGateway;
import com.fiap.estoque.core.usecase.AtualizarPecaUseCase;
import com.fiap.estoque.core.usecase.CriarPecaUseCase;
import com.fiap.estoque.core.usecase.DeletarPecaUseCase;
import com.fiap.estoque.core.usecase.ListarPecasUseCase;
import com.fiap.estoque.infra.controller.json.PecaResponseJson;
import com.fiap.estoque.infra.controller.presenter.ListarPecasPresenter;
import com.fiap.estoque.shared.page.Pagina;

public class PecaCleanController {

    private final PecaGateway pecaGateway;

    public PecaCleanController(PecaGateway pecaGateway) {
        this.pecaGateway = pecaGateway;
    }

    public void criar(CriarPecaDto dto) {
        new CriarPecaUseCase(pecaGateway).criar(dto);
    }

    public Pagina<PecaResponseJson> listar(ListarPecasDto dto) {
        var presenter = new ListarPecasPresenter();
        new ListarPecasUseCase(pecaGateway, presenter).listar(dto);
        return presenter.getViewModel();
    }

    public void atualizar(AtualizarPecaDto dto) {
        new AtualizarPecaUseCase(pecaGateway).atualizar(dto);
    }

    public void deletar(Long id) {
        new DeletarPecaUseCase(pecaGateway).deletar(id);
    }
}
