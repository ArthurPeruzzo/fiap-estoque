package com.fiap.estoque.infra.mensageria;

/** Corpo da mensagem enviada pelo fiap-mecanica depois do pagamento: dê baixa definitiva nas reservas desta ordem de serviço. */
public record ConfirmarBaixaComando(Long ordemServicoId) {
}
