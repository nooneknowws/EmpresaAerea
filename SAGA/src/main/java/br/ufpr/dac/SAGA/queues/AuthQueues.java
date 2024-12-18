package br.ufpr.dac.SAGA.queues;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthQueues{
	private static final int QUEUE_TTL = 30000; 
	/*
     * Authentication Related Queues
     * These queues handle the existing authentication flow
     */
    @Bean
    Queue authRequestQueue() {
        return QueueBuilder.durable("auth.request")
                .ttl(QUEUE_TTL)
                .build();
    }
    
    @Bean
    Queue authResponseQueue() {
        return QueueBuilder.durable("auth.response")
                .ttl(QUEUE_TTL)
                .build();
    }

    @Bean
    Queue clientVerificationQueue() {
        return QueueBuilder.durable("client.verification")
                .ttl(QUEUE_TTL)
                .build();
    }

    @Bean
    Queue employeeVerificationQueue() {
        return new Queue("employee.verification");
    }

    @Bean
    Queue clientResponseQueue() {
        return QueueBuilder.durable("client.verification.response")
                .ttl(QUEUE_TTL)
                .build();
    }

    @Bean
    Queue employeeVerificationResponseQueue() {
        return new Queue("employee.verification.response");
    }
    
    /*
     * Authentication Flow Bindings
     */
    
    @Bean
    Binding authRequestBinding(@Qualifier("authRequestQueue") Queue authRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(authRequestQueue).to(exchange).with("auth.request");
    }

    @Bean
    Binding authResponseBinding(@Qualifier("authResponseQueue") Queue authResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(authResponseQueue).to(exchange).with("auth.response");
    }
    
    @Bean
    Binding clientRegistrationResponseBinding(@Qualifier("clientRegistrationResponseQueue") Queue clientRegistrationResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(clientRegistrationResponseQueue).to(exchange).with("client.registration.response");
    }
    
    @Bean
    Binding employeeRegistrationResponseBinding(@Qualifier("employeeRegistrationResponseQueue") Queue employeeRegistrationResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(employeeRegistrationResponseQueue).to(exchange).with("employee.registration.response");
    }
    
    @Bean
    Binding clientVerificationBinding(@Qualifier("clientVerificationQueue") Queue clientVerificationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(clientVerificationQueue).to(exchange).with("client.verification");
    }

    @Bean
    Binding clientResponseBinding(@Qualifier("clientResponseQueue") Queue clientResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(clientResponseQueue).to(exchange).with("client.verification.response");
    }

    @Bean
    Binding employeeVerificationBinding(@Qualifier("employeeVerificationQueue") Queue employeeVerificationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(employeeVerificationQueue).to(exchange).with("employee.verification");
    }

    @Bean
    Binding employeeVerificationResponseBinding(@Qualifier("employeeVerificationResponseQueue") Queue employeeVerificationResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(employeeVerificationResponseQueue).to(exchange).with("employee.verification.response");
    }

}
