package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.dto.ItemCatalogoDto;
import com.fiap.estoque.core.gateway.PecaGateway;

import java.util.List;

/**
 * Consulta em lote das peças (id e preço) para quem monta orçamentos. Ids inexistentes ficam de fora:
 * quem consulta decide o que fazer com a diferença.
 */
public class ConsultarCatalogoPecasUseCase {

	private final PecaGateway pecaGateway;
	private final ConsultarCatalogoOutputPort outputPort;

	public ConsultarCatalogoPecasUseCase(PecaGateway pecaGateway, ConsultarCatalogoOutputPort outputPort) {
		this.pecaGateway = pecaGateway;
		this.outputPort = outputPort;
	}

	public void consultar(List<Long> ids) {
		outputPort.apresentar(pecaGateway.listarPorIds(ids).stream()
				.map(peca -> new ItemCatalogoDto(peca.getId(), peca.getPreco()))
				.toList());
	}
}
