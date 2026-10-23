package com.fiap.estoque.core.dto;

import com.fiap.estoque.core.domain.UnidadeMedida;

import java.math.BigDecimal;

public record AtualizarInsumoDto(Long id, String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
}
