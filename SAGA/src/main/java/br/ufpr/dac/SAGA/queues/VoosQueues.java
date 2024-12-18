package br.ufpr.dac.SAGA.queues;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class VoosQueues {
    
    private static final long QUEUE_TTL = 60 * 1000;

    //FILAS
    @Bean
    Queue vooStatusRequestQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("voo.status.request", true, false, false, args);
    }

    @Bean
    Queue vooStatusResponseQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("voo.status.response", true, false, false, args);
    }

    @Bean
    Queue vooReservaAtualizacaoQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("voo.reserva.atualizacao", true, false, false, args);
    }

    @Bean
    Queue vooReservaResponseQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("voo.reserva.response", true, false, false, args);
    }

    //BINDINGS
    @Bean
    Binding vooStatusRequestBinding(
            @Qualifier("vooStatusRequestQueue") Queue vooStatusRequestQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(vooStatusRequestQueue)
            .to(exchange)
            .with("voo.status.request");
    }

    @Bean
    Binding vooStatusResponseBinding(
            @Qualifier("vooStatusResponseQueue") Queue vooStatusResponseQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(vooStatusResponseQueue)
            .to(exchange)
            .with("voo.status.response");
    }

    @Bean
    Binding vooReservaAtualizacaoBinding(
            @Qualifier("vooReservaAtualizacaoQueue") Queue vooReservaAtualizacaoQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(vooReservaAtualizacaoQueue)
            .to(exchange)
            .with("voo.reserva.atualizacao");
    }

    @Bean
    Binding vooReservaResponseBinding(
            @Qualifier("vooReservaResponseQueue") Queue vooReservaResponseQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(vooReservaResponseQueue)
            .to(exchange)
            .with("voo.reserva.response");
    }
}