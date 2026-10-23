package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.dto.AtualizarInsumoDto;
import com.fiap.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.estoque.core.gateway.InsumoGateway;

public class AtualizarInsumoUseCase {

    private final InsumoGateway insumoGateway;

    public AtualizarInsumoUseCase(InsumoGateway insumoGateway) {
        this.insumoGateway = insumoGateway;
    }

    public void atualizar(AtualizarInsumoDto dto) {
        var insumo = insumoGateway.buscarPorId(dto.id()).orElseThrow(InsumoNaoEncontradoException::new);
        insumo.atualizar(dto.nome(), dto.descricao(), dto.preco(), dto.unidadeMedida(), dto.quantidadeEstoque());
        insumoGateway.atualizar(insumo);
    }
}
