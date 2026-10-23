package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.estoque.core.gateway.PecaGateway;

public class DeletarPecaUseCase {

    private final PecaGateway pecaGateway;

    public DeletarPecaUseCase(PecaGateway pecaGateway) {
        this.pecaGateway = pecaGateway;
    }

    public void deletar(Long id) {
        pecaGateway.buscarPorId(id).orElseThrow(PecaNaoEncontradaException::new);
        pecaGateway.deletar(id);
    }
}
