package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.ItemEstoque;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;

/**
 * Devolve ao saldo disponível tudo que estava reservado para a ordem de serviço.
 */
public class LiberarReservasOrdemDeServicoUseCase {

	private final ItemEstoqueGateway itemEstoqueGateway;
	private final ReservaGateway reservaGateway;
	private final TransacaoGateway transacaoGateway;

	public LiberarReservasOrdemDeServicoUseCase(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway,
														 TransacaoGateway transacaoGateway) {
		this.itemEstoqueGateway = itemEstoqueGateway;
		this.reservaGateway = reservaGateway;
		this.transacaoGateway = transacaoGateway;
	}

	public void liberar(Long ordemServicoId) {
		transacaoGateway.executar(() -> reservaGateway.listarAtivasPorOrdemDeServico(ordemServicoId).forEach(reserva -> {
			ItemEstoque item = itemEstoqueGateway.buscar(reserva.getTipoItem(), reserva.getItemId());
			item.liberarReserva(reserva.getQuantidade());
			reserva.liberar();
			itemEstoqueGateway.atualizar(item);
			reservaGateway.salvar(reserva);
		}));
	}
}
