package com.fiap.estoque.core.dto;

import java.math.BigDecimal;

public record CriarPecaDto(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {
}
