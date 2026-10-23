package com.fiap.estoque.infra.controller.json;

import com.fiap.estoque.core.domain.UnidadeMedida;

import java.math.BigDecimal;

public record InsumoResponseJson(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, UnidadeMedida unidadeMedida) {}
