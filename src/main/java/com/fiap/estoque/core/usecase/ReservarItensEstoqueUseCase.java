package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.dto.ItemReservaDto;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;

import java.util.List;

/**
 * Define, em lote, a quantidade total reservada de cada item para uma ordem de serviço.
 * Tudo ou nada: roda numa única transação, então a falha em qualquer item desfaz as reservas
 * dos anteriores. Idempotente, como {@link ReservarItemEstoqueUseCase}.
 */
public class ReservarItensEstoqueUseCase {

	private final ReservarItemEstoqueUseCase reservarItemEstoqueUseCase;
	private final TransacaoGateway transacaoGateway;

	public ReservarItensEstoqueUseCase(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway,
									   TransacaoGateway transacaoGateway) {
		this.transacaoGateway = transacaoGateway;
		this.reservarItemEstoqueUseCase = new ReservarItemEstoqueUseCase(itemEstoqueGateway, reservaGateway);
	}

	public void reservar(Long ordemServicoId, List<ItemReservaDto> itens) {
		transacaoGateway.executar(() -> itens.forEach(item -> reservarItemEstoqueUseCase.reservar(
				ordemServicoId, item.tipoItem(), item.itemId(), item.quantidadeTotal())));
	}
}
