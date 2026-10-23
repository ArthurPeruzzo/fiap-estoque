package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.dto.CriarInsumoDto;
import com.fiap.estoque.core.gateway.InsumoGateway;

public class CriarInsumoUseCase {

    private final InsumoGateway insumoGateway;

    public CriarInsumoUseCase(InsumoGateway insumoGateway) {
        this.insumoGateway = insumoGateway;
    }

    public void criar(CriarInsumoDto dto) {
        Insumo insumo = new Insumo(dto.nome(), dto.descricao(), dto.preco(), dto.unidadeMedida(), dto.quantidadeEstoque());
        insumoGateway.criar(insumo);
    }
}
