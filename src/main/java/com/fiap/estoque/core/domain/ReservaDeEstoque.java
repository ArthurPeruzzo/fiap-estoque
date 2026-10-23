package com.fiap.estoque.core.domain;

/**
 * Domain service: ajusta a reserva de uma ordem de serviço para um novo total e
 * move a diferença entre o disponível e o reservado do item de estoque.
 */
public class ReservaDeEstoque {

	/**
	 * @return true se algo mudou (e portanto item e reserva precisam ser persistidos);
	 * false se o total já era o da reserva ativa (operação idempotente).
	 */
	public boolean definirQuantidade(ItemEstoque item, Reserva reserva, Integer novoTotal) {
		int totalJaReservado = reserva.quantidadeAtiva();
		if (novoTotal == totalJaReservado) return false;

		reserva.definirQuantidade(novoTotal);
		if (novoTotal > totalJaReservado) {
			item.reservar(novoTotal - totalJaReservado);
		} else {
			item.liberarReserva(totalJaReservado - novoTotal);
		}
		return true;
	}
}
