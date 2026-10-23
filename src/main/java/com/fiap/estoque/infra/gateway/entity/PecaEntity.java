package com.fiap.estoque.infra.gateway.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="peca")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PecaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false)
	private String nome;

	@Column(name = "descricao", nullable = false)
	private String descricao;

	@Column(name = "preco", nullable = false)
	private BigDecimal preco;

	@Column(name = "quantidade_estoque", nullable = false)
	private Integer quantidadeEstoque;

	@Column(name = "quantidade_reservada", nullable = false)
	@Builder.Default
	private Integer quantidadeReservada = 0;

}
