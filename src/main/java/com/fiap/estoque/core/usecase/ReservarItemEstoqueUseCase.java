package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.ItemEstoque;
import com.fiap.estoque.core.domain.Reserva;
import com.fiap.estoque.core.domain.ReservaDeEstoque;
import com.fiap.estoque.core.domain.TipoItemReserva;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;

/**
 * Define a quantidade total reservada de um item para uma ordem de serviço.
 * Idempotente: repetir a chamada com o mesmo total não altera o estoque.
 */
public class ReservarItemEstoqueUseCase {

	private final ItemEstoqueGateway itemEstoqueGateway;
	private final ReservaGateway reservaGateway;
	private final ReservaDeEstoque reservaDeEstoque = new ReservaDeEstoque();

	public ReservarItemEstoqueUseCase(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway) {
		this.itemEstoqueGateway = itemEstoqueGateway;
		this.reservaGateway = reservaGateway;
	}

	public void reservar(Long ordemServicoId, TipoItemReserva tipoItem, Long itemId, Integer quantidadeTotal) {
		ItemEstoque item = itemEstoqueGateway.buscar(tipoItem, itemId);
		Reserva reserva = reservaGateway.buscar(ordemServicoId, tipoItem, itemId)
				.orElseGet(() -> new Reserva(ordemServicoId, tipoItem, itemId));

		if (!reservaDeEstoque.definirQuantidade(item, reserva, quantidadeTotal)) return;

		itemEstoqueGateway.atualizar(item);
		reservaGateway.salvar(reserva);
	}
}
