package com.fiap.estoque.core.dto;

import com.fiap.estoque.core.domain.UnidadeMedida;

import java.math.BigDecimal;

public record CriarInsumoDto(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, UnidadeMedida unidadeMedida) {
}
