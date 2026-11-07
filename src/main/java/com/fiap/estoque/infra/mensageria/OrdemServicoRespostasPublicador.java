package com.fiap.estoque.infra.mensageria;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Responde ao orquestrador (fiap-mecanica) no exchange {@code ordemservico.eventos}. Só retorna depois que o broker
 * confirma que a mensagem chegou a uma fila; sem confirmação, ou sem fila ligada à chave, lança exceção.
 * <p>
 * Não usa outbox: quem chama é um listener cuja ação (confirmar ou liberar reservas) é idempotente, então a falha ao
 * responder faz a mensagem de comando ser reprocessada (retry e, no fim, DLQ) e a resposta sai de novo.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "estoque.mensageria.listener.enabled", havingValue = "true", matchIfMissing = true)
public class OrdemServicoRespostasPublicador {

    static final String TIPO_BAIXA_CONFIRMADA = "BAIXA_CONFIRMADA";
    static final String TIPO_RESERVA_LIBERADA = "RESERVA_LIBERADA";

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKeyBaixaConfirmada;
    private final String routingKeyReservaLiberada;
    private final Duration timeoutDaConfirmacao;

    public OrdemServicoRespostasPublicador(RabbitTemplate rabbitTemplate,
                                           @Value("${ordemservico.mensageria.exchange}") String exchange,
                                           @Value("${ordemservico.mensageria.routing-key.baixa-confirmada}") String routingKeyBaixaConfirmada,
                                           @Value("${ordemservico.mensageria.routing-key.reserva-liberada}") String routingKeyReservaLiberada,
                                           @Value("${estoque.mensageria.timeout-confirmacao:5s}") Duration timeoutDaConfirmacao) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKeyBaixaConfirmada = routingKeyBaixaConfirmada;
        this.routingKeyReservaLiberada = routingKeyReservaLiberada;
        this.timeoutDaConfirmacao = timeoutDaConfirmacao;
    }

    public void baixaConfirmada(Long ordemServicoId) {
        publicar(TIPO_BAIXA_CONFIRMADA, routingKeyBaixaConfirmada, ordemServicoId);
    }

    public void reservaLiberada(Long ordemServicoId) {
        publicar(TIPO_RESERVA_LIBERADA, routingKeyReservaLiberada, ordemServicoId);
    }

    private void publicar(String tipo, String routingKey, Long ordemServicoId) {
        String id = UUID.randomUUID().toString();
        Message mensagem = MessageBuilder
                .withBody(("{\"ordemServicoId\":" + ordemServicoId + "}").getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(id)
                .setType(tipo)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        CorrelationData correlacao = new CorrelationData(id);
        rabbitTemplate.send(exchange, routingKey, mensagem, correlacao);
        try {
            CorrelationData.Confirm confirmacao = correlacao.getFuture().get(timeoutDaConfirmacao.toMillis(), TimeUnit.MILLISECONDS);
            if (!confirmacao.ack()) {
                throw new IllegalStateException("broker recusou a resposta " + tipo + ": " + confirmacao.reason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrompido ao esperar a confirmacao do broker", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("sem confirmacao do broker para a resposta " + tipo, e);
        }
        if (correlacao.getReturned() != null) {
            // Confirmada pelo broker, mas sem fila ligada à chave (a OS ainda não declarou a fila de respostas).
            throw new IllegalStateException("resposta " + tipo + " nao roteada: " + correlacao.getReturned().getReplyText());
        }
        log.info("Resposta {} da ordem de servico {} publicada em {}", tipo, ordemServicoId, exchange);
    }
}
