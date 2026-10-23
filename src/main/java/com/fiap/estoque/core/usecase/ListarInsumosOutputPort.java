package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.shared.page.Pagina;

public interface ListarInsumosOutputPort {
    void apresentar(Pagina<Insumo> pagina);
}
