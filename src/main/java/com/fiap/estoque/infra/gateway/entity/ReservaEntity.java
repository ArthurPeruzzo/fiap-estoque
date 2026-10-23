package com.fiap.estoque.infra.gateway.entity;

import com.fiap.estoque.core.domain.StatusReserva;
import com.fiap.estoque.core.domain.TipoItemReserva;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva_estoque")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ReservaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ordem_servico_id", nullable = false)
	private Long ordemServicoId;

	@Column(name = "tipo_item", nullable = false)
	@Enumerated(EnumType.STRING)
	private TipoItemReserva tipoItem;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	@Column(name = "quantidade", nullable = false)
	private Integer quantidade;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusReserva status;
}
