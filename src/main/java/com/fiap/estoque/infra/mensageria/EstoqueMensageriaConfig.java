package com.fiap.estoque.infra.mensageria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia do lado do consumidor: a fila de comandos, a DLQ para o que esgotar as tentativas e as ligações.
 * O exchange principal é declarado também pelo fiap-mecanica, com os mesmos argumentos.
 */
@Configuration
@ConditionalOnProperty(name = "estoque.mensageria.listener.enabled", havingValue = "true", matchIfMissing = true)
public class EstoqueMensageriaConfig {

    @Bean
    DirectExchange estoqueComandosExchange(@Value("${estoque.mensageria.exchange}") String nome) {
        return new DirectExchange(nome, true, false);
    }

    @Bean
    DirectExchange estoqueComandosDeadLetterExchange(@Value("${estoque.mensageria.exchange}") String nome) {
        return new DirectExchange(nome + ".dlx", true, false);
    }

    @Bean
    Queue filaLiberarReserva(@Value("${estoque.mensageria.fila.liberar-reserva}") String nome,
                             @Value("${estoque.mensageria.exchange}") String exchange,
                             @Value("${estoque.mensageria.routing-key.liberar-reserva}") String routingKey) {
        return QueueBuilder.durable(nome)
                .deadLetterExchange(exchange + ".dlx")
                .deadLetterRoutingKey(routingKey)
                .build();
    }

    @Bean
    Queue filaLiberarReservaDlq(@Value("${estoque.mensageria.fila.liberar-reserva}") String nome) {
        return QueueBuilder.durable(nome + ".dlq").build();
    }

    @Bean
    Binding bindingLiberarReserva(Queue filaLiberarReserva, DirectExchange estoqueComandosExchange,
                                  @Value("${estoque.mensageria.routing-key.liberar-reserva}") String routingKey) {
        return BindingBuilder.bind(filaLiberarReserva).to(estoqueComandosExchange).with(routingKey);
    }

    @Bean
    Binding bindingLiberarReservaDlq(Queue filaLiberarReservaDlq, DirectExchange estoqueComandosDeadLetterExchange,
                                     @Value("${estoque.mensageria.routing-key.liberar-reserva}") String routingKey) {
        return BindingBuilder.bind(filaLiberarReservaDlq).to(estoqueComandosDeadLetterExchange).with(routingKey);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
