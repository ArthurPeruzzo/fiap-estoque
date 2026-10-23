package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.ItemEstoque;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;

/**
 * Devolve ao saldo disponível tudo que estava reservado para a ordem de serviço.
 */
public class LiberarReservasOrdemDeServicoUseCase {

	private final ItemEstoqueGateway itemEstoqueGateway;
	private final ReservaGateway reservaGateway;

	public LiberarReservasOrdemDeServicoUseCase(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway) {
		this.itemEstoqueGateway = itemEstoqueGateway;
		this.reservaGateway = reservaGateway;
	}

	public void liberar(Long ordemServicoId) {
		reservaGateway.listarAtivasPorOrdemDeServico(ordemServicoId).forEach(reserva -> {
			ItemEstoque item = itemEstoqueGateway.buscar(reserva.getTipoItem(), reserva.getItemId());
			item.liberarReserva(reserva.getQuantidade());
			reserva.liberar();
			itemEstoqueGateway.atualizar(item);
			reservaGateway.salvar(reserva);
		});
	}
}
