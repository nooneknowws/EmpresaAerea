package br.ufpr.dac.SAGA.queues;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ReservasQueue {
    
    // TTL value in milliseconds (60 seconds = 60 * 1000)
    private static final long QUEUE_TTL = 60 * 1000;

    //FILAS
    @Bean
    Queue reservaCancelamentoRequestQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("reserva.cancelamento.request", true, false, false, args);
    }

    @Bean
    Queue reservaCancelamentoResponseQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("reserva.cancelamento.response", true, false, false, args);
    }

    @Bean
    Queue milhasProcessamentoQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("milhas.processamento", true, false, false, args);
    }

    @Bean
    Queue milhasProcessamentoResponseQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("milhas.processamento.response", true, false, false, args);
    }

    @Bean
    Queue reservaCancellationCompleteQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("reserva.cancellation.complete", true, false, false, args);
    }
    @Bean
    Queue reservaCriacaoRequestQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("reserva.criacao.request", true, false, false, args);
    }

    @Bean
    Queue reservaCriacaoResponseQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("reserva.criacao.response", true, false, false, args);
    }

    @Bean
    Queue vooAtualizacaoQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("voo.atualizacao", true, false, false, args);
    }

    @Bean
    Queue vooAtualizacaoResponseQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", QUEUE_TTL);
        return new Queue("voo.atualizacao.response", true, false, false, args);
    }


    //BINDINGS
    @Bean
    Binding reservaCancelamentoRequestBinding(
            @Qualifier("reservaCancelamentoRequestQueue") Queue reservaCancelamentoRequestQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(reservaCancelamentoRequestQueue)
            .to(exchange)
            .with("reserva.cancelamento.request");
    }

    @Bean
    Binding reservaCancelamentoResponseBinding(
            @Qualifier("reservaCancelamentoResponseQueue") Queue reservaCancelamentoResponseQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(reservaCancelamentoResponseQueue)
            .to(exchange)
            .with("reserva.cancelamento.response");
    }

    @Bean
    Binding milhasProcessamentoBinding(
            @Qualifier("milhasProcessamentoQueue") Queue milhasProcessamentoQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(milhasProcessamentoQueue)
            .to(exchange)
            .with("milhas.processamento");
    }

    @Bean
    Binding milhasProcessamentoResponseBinding(
            @Qualifier("milhasProcessamentoResponseQueue") Queue milhasProcessamentoResponseQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(milhasProcessamentoResponseQueue)
            .to(exchange)
            .with("milhas.processamento.response");
    }

    @Bean
    Binding reservaCancellationCompleteBinding(
            @Qualifier("reservaCancellationCompleteQueue") Queue reservaCancellationCompleteQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(reservaCancellationCompleteQueue)
            .to(exchange)
            .with("reserva.cancellation.complete");
    }
    @Bean
    Binding reservaCriacaoRequestBinding(
            @Qualifier("reservaCriacaoRequestQueue") Queue reservaCriacaoRequestQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(reservaCriacaoRequestQueue)
            .to(exchange)
            .with("reserva.criacao.request");
    }

    @Bean
    Binding reservaCriacaoResponseBinding(
            @Qualifier("reservaCriacaoResponseQueue") Queue reservaCriacaoResponseQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(reservaCriacaoResponseQueue)
            .to(exchange)
            .with("reserva.criacao.response");
    }

    @Bean
    Binding vooAtualizacaoBinding(
            @Qualifier("vooAtualizacaoQueue") Queue vooAtualizacaoQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(vooAtualizacaoQueue)
            .to(exchange)
            .with("voo.atualizacao");
    }

    @Bean
    Binding vooAtualizacaoResponseBinding(
            @Qualifier("vooAtualizacaoResponseQueue") Queue vooAtualizacaoResponseQueue,
            @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder
            .bind(vooAtualizacaoResponseQueue)
            .to(exchange)
            .with("voo.atualizacao.response");
    }
}