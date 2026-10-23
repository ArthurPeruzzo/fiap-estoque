package com.fiap.estoque.infra.controller.presenter;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.usecase.ListarPecasOutputPort;
import com.fiap.estoque.infra.controller.json.PecaResponseJson;
import com.fiap.estoque.shared.page.Pagina;

public class ListarPecasPresenter implements ListarPecasOutputPort {

    private Pagina<PecaResponseJson> viewModel;

    @Override
    public void apresentar(Pagina<Peca> pagina) {
        this.viewModel = pagina.map(peca -> new PecaResponseJson(
                peca.getId(),
                peca.getNome(),
                peca.getDescricao(),
                peca.getPreco(),
                peca.getEstoqueTotal()
        ));
    }

    public Pagina<PecaResponseJson> getViewModel() {
        return viewModel;
    }
}
