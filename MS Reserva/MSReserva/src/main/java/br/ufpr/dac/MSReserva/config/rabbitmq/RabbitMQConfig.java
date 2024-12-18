package br.ufpr.dac.MSReserva.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
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