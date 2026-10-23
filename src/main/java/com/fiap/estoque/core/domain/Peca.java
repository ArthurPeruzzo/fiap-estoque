package com.fiap.estoque.core.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class Peca extends ItemEstoque {
	private Long id;
	private String nome;
	private String descricao;
	private BigDecimal preco;

	public Peca(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {
		super(quantidadeEstoque);
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
	}

	public void atualizar(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
		ajustarQuantidade(quantidadeEstoque);
	}
}
