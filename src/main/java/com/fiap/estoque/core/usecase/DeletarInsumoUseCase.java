package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.estoque.core.gateway.InsumoGateway;

public class DeletarInsumoUseCase {

    private final InsumoGateway insumoGateway;

    public DeletarInsumoUseCase(InsumoGateway insumoGateway) {
        this.insumoGateway = insumoGateway;
    }

    public void deletar(Long id) {
        insumoGateway.buscarPorId(id).orElseThrow(InsumoNaoEncontradoException::new);
        insumoGateway.deletar(id);
    }
}
