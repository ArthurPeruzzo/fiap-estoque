package com.fiap.estoque.core.gateway;

/**
 * Limite de transação do banco local: tudo que roda dentro de {@code executar} é confirmado
 * junto ou desfeito junto.
 */
public interface TransacaoGateway {
    void executar(Runnable acao);
}
