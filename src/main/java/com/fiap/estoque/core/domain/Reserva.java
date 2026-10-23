package com.fiap.estoque.core.domain;

import com.fiap.estoque.core.exception.ReservaJaConfirmadaException;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reserva {
	private Long id;
	private final Long ordemServicoId;
	private final TipoItemReserva tipoItem;
	private final Long itemId;
	private Integer quantidade;
	private StatusReserva status;

	public Reserva(Long ordemServicoId, TipoItemReserva tipoItem, Long itemId) {
		this.ordemServicoId = ordemServicoId;
		this.tipoItem = tipoItem;
		this.itemId = itemId;
		this.quantidade = 0;
		this.status = StatusReserva.ATIVA;
	}

	public boolean isAtiva() {
		return StatusReserva.ATIVA.equals(status);
	}

	public Integer quantidadeAtiva() {
		return isAtiva() ? quantidade : 0;
	}

	public void definirQuantidade(Integer quantidadeTotal) {
		if (!isAtiva() && !StatusReserva.LIBERADA.equals(status)) {
			throw new ReservaJaConfirmadaException();
		}
		this.quantidade = quantidadeTotal;
		this.status = quantidadeTotal > 0 ? StatusReserva.ATIVA : StatusReserva.LIBERADA;
	}

	public void confirmar() {
		this.status = StatusReserva.CONFIRMADA;
	}

	public void liberar() {
		this.status = StatusReserva.LIBERADA;
	}
}
