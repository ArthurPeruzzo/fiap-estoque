package com.fiap.estoque.core.usecase;

import com.fiap.estoque.core.dto.ItemCatalogoDto;
import com.fiap.estoque.core.gateway.InsumoGateway;

import java.util.List;

/**
 * Consulta em lote dos insumos (id e preço) para quem monta orçamentos. Ids inexistentes ficam de fora:
 * quem consulta decide o que fazer com a diferença.
 */
public class ConsultarCatalogoInsumosUseCase {

	private final InsumoGateway insumoGateway;
	private final ConsultarCatalogoOutputPort outputPort;

	public ConsultarCatalogoInsumosUseCase(InsumoGateway insumoGateway, ConsultarCatalogoOutputPort outputPort) {
		this.insumoGateway = insumoGateway;
		this.outputPort = outputPort;
	}

	public void consultar(List<Long> ids) {
		outputPort.apresentar(insumoGateway.listarPorIds(ids).stream()
				.map(insumo -> new ItemCatalogoDto(insumo.getId(), insumo.getPreco()))
				.toList());
	}
}
