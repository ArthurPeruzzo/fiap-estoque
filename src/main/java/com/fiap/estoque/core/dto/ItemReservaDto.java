package com.fiap.estoque.core.dto;

import com.fiap.estoque.core.domain.TipoItemReserva;

public record ItemReservaDto(TipoItemReserva tipoItem, Long itemId, Integer quantidadeTotal) {
}
