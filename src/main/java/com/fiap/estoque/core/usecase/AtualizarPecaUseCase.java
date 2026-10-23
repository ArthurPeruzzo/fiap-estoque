package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.dto.AtualizarPecaDto;
import com.fiap.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.estoque.core.gateway.PecaGateway;

public class AtualizarPecaUseCase {

    private final PecaGateway pecaGateway;

    public AtualizarPecaUseCase(PecaGateway pecaGateway) {
        this.pecaGateway = pecaGateway;
    }

    public void atualizar(AtualizarPecaDto dto) {
        var peca = pecaGateway.buscarPorId(dto.id()).orElseThrow(PecaNaoEncontradaException::new);
        peca.atualizar(dto.nome(), dto.descricao(), dto.preco(), dto.quantidadeEstoque());
        pecaGateway.atualizar(peca);
    }
}
