package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.dto.ItemCatalogoDto;

import java.util.List;

public interface ConsultarCatalogoOutputPort {
    void apresentar(List<ItemCatalogoDto> itens);
}
