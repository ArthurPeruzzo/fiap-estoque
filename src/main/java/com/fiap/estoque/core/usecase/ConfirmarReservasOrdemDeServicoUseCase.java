package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.domain.ItemEstoque;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;

/**
 * Converte as reservas ativas da ordem de serviço em baixa definitiva do estoque.
 */
public class ConfirmarReservasOrdemDeServicoUseCase {

	private final ItemEstoqueGateway itemEstoqueGateway;
	private final ReservaGateway reservaGateway;

	public ConfirmarReservasOrdemDeServicoUseCase(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway) {
		this.itemEstoqueGateway = itemEstoqueGateway;
		this.reservaGateway = reservaGateway;
	}

	public void confirmar(Long ordemServicoId) {
		reservaGateway.listarAtivasPorOrdemDeServico(ordemServicoId).forEach(reserva -> {
			ItemEstoque item = itemEstoqueGateway.buscar(reserva.getTipoItem(), reserva.getItemId());
			item.confirmarBaixa(reserva.getQuantidade());
			reserva.confirmar();
			itemEstoqueGateway.atualizar(item);
			reservaGateway.salvar(reserva);
		});
	}
}
