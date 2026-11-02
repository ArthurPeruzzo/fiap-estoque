package com.fiap.estoque.infra.mensageria;

/** Corpo da mensagem enviada pelo fiap-mecanica: libere as reservas desta ordem de serviço. */
public record LiberarReservaComando(Long ordemServicoId) {
}
