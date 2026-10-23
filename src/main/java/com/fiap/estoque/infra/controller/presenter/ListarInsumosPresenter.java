package com.fiap.estoque.infra.controller.presenter;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.usecase.ListarInsumosOutputPort;
import com.fiap.estoque.infra.controller.json.InsumoResponseJson;
import com.fiap.estoque.shared.page.Pagina;

public class ListarInsumosPresenter implements ListarInsumosOutputPort {

    private Pagina<InsumoResponseJson> viewModel;

    @Override
    public void apresentar(Pagina<Insumo> pagina) {
        this.viewModel = pagina.map(insumo -> new InsumoResponseJson(
                insumo.getId(),
                insumo.getNome(),
                insumo.getDescricao(),
                insumo.getPreco(),
                insumo.getEstoqueTotal(),
                insumo.getUnidadeMedida()
        ));
    }

    public Pagina<InsumoResponseJson> getViewModel() {
        return viewModel;
    }
}
