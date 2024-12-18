package br.ufpr.dac.SAGA.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitConfig {
    
    public static final String EXCHANGE_NAME = "reserva.events";
    public static final String QUEUE_NAME = "reserva.sync.queue";
    public static final String ROUTING_KEY = "reserva.sync.#";

    @Bean
    TopicExchange reservaExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue reservaQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    Binding binding() {
        return BindingBuilder
            .bind(reservaQueue())
            .to(reservaExchange())
            .with(ROUTING_KEY);
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}