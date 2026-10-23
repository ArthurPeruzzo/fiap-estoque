package com.fiap.estoque.core.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class Insumo extends ItemEstoque {
	private Long id;
	private String nome;
	private String descricao;
	private BigDecimal preco;
	private UnidadeMedida unidadeMedida;

	public Insumo(String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
		super(quantidadeEstoque);
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
		this.unidadeMedida = unidadeMedida;
	}

	public void atualizar(String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
		this.unidadeMedida = unidadeMedida;
		ajustarQuantidade(quantidadeEstoque);
	}
}
