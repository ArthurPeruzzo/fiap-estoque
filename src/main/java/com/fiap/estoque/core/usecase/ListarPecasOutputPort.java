package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.shared.page.Pagina;

public interface ListarPecasOutputPort {
    void apresentar(Pagina<Peca> pagina);
}
