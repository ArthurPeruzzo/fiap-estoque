package com.fiap.estoque.core.gateway;

import com.fiap.estoque.core.domain.ItemEstoque;
import com.fiap.estoque.core.domain.TipoItemReserva;

public interface ItemEstoqueGateway {
    /**
     * @throws com.fiap.estoque.core.exception.PecaNaoEncontradaException    se o tipo for PECA e o item não existir
     * @throws com.fiap.estoque.core.exception.InsumoNaoEncontradoException se o tipo for INSUMO e o item não existir
     */
    ItemEstoque buscar(TipoItemReserva tipoItem, Long itemId);

    void atualizar(ItemEstoque item);
}
