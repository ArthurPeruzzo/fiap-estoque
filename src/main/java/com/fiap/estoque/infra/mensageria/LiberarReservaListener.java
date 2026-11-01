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
 * Consome o comando "liberar reservas da OS". A entrega do broker é "pelo menos uma vez", então a mesma
 * mensagem pode chegar mais de uma vez; isso é inofensivo porque a liberação só age em reservas ATIVAS.
 * Falha do banco propaga e aciona o retry do listener; esgotadas as tentativas a mensagem vai para a DLQ.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "estoque.mensageria.listener.enabled", havingValue = "true", matchIfMissing = true)
public class LiberarReservaListener {

    private final ReservaCleanController cleanController;

    public LiberarReservaListener(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway,
                                  TransacaoGateway transacaoGateway) {
        this.cleanController = new ReservaCleanController(itemEstoqueGateway, reservaGateway, transacaoGateway);
    }

    @RabbitListener(queues = "${estoque.mensageria.fila.liberar-reserva}")
    public void liberar(LiberarReservaComando comando) {
        if (comando == null || comando.ordemServicoId() == null) {
            // Mensagem malformada nunca vai passar numa nova tentativa: segue direto para a DLQ.
            throw new AmqpRejectAndDontRequeueException("Comando de liberacao sem ordemServicoId");
        }
        log.info("Liberando reservas da ordem de servico {} (comando assincrono)", comando.ordemServicoId());
        cleanController.liberar(comando.ordemServicoId());
    }
}
