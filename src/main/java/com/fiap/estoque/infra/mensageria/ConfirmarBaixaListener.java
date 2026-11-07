package com.fiap.estoque.infra.mensageria;

import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;
import com.fiap.estoque.infra.controller.ReservaCleanController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consome o comando "confirmar a baixa das reservas da OS" (enviado depois do pagamento aprovado) e responde
 * {@code BaixaConfirmada} ao orquestrador. A entrega é "pelo menos uma vez": confirmar só age em reservas ATIVAS, então a
 * repetição não baixa o estoque duas vezes, e a resposta é reenviada mesmo assim (a anterior pode ter se perdido).
 * Falha do banco ou da resposta propaga e aciona o retry; esgotadas as tentativas a mensagem vai para a DLQ.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "estoque.mensageria.listener.enabled", havingValue = "true", matchIfMissing = true)
public class ConfirmarBaixaListener {

    private final ReservaCleanController cleanController;
    private final OrdemServicoRespostasPublicador respostas;

    public ConfirmarBaixaListener(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway,
                                  TransacaoGateway transacaoGateway, OrdemServicoRespostasPublicador respostas) {
        this.cleanController = new ReservaCleanController(itemEstoqueGateway, reservaGateway, transacaoGateway);
        this.respostas = respostas;
    }

    @RabbitListener(queues = "${estoque.mensageria.fila.confirmar-baixa}")
    public void confirmar(ConfirmarBaixaComando comando) {
        if (comando == null || comando.ordemServicoId() == null) {
            // Mensagem malformada nunca vai passar numa nova tentativa: segue direto para a DLQ.
            throw new AmqpRejectAndDontRequeueException("Comando de baixa sem ordemServicoId");
        }
        log.info("Confirmando a baixa das reservas da ordem de servico {} (comando assincrono)", comando.ordemServicoId());
        cleanController.confirmar(comando.ordemServicoId());
        respostas.baixaConfirmada(comando.ordemServicoId());
    }
}
