package com.fiap.estoque.core.dto;

import java.math.BigDecimal;

public record AtualizarPecaDto(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {
}
