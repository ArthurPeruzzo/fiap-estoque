package com.fiap.estoque.infra.gateway;

import com.fiap.estoque.core.gateway.TransacaoGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TransacaoSpringGateway implements TransacaoGateway {

	private final TransactionTemplate transactionTemplate;

	public TransacaoSpringGateway(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public void executar(Runnable acao) {
		transactionTemplate.executeWithoutResult(status -> acao.run());
	}
}
