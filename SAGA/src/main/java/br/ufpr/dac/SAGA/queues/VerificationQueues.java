package br.ufpr.dac.SAGA.queues;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerificationQueues {
	private static final int QUEUE_TTL = 30000; 
	
	/*
     * Email Verification Related Queues
     * These queues handle the new email verification flow
     */
	
    @Bean
    Queue registrationRequestQueue() {
    	return QueueBuilder.durable("registration.request")
    			.ttl(QUEUE_TTL)
    			.build();
    }
    @Bean
    Queue clientRegistrationResponseQueue() {
    	return QueueBuilder.durable("client.registration.response")
    			.ttl(QUEUE_TTL)
    			.build();
    }
    
    @Bean
    Queue employeeRegistrationResponseQueue() {
    	return QueueBuilder.durable("employee.registration.response")
    			.ttl(QUEUE_TTL)
    			.build();
    }
    
    @Bean
    Queue emailVerificationRequestQueue() {
        return QueueBuilder.durable("email.verification.request")
                .ttl(QUEUE_TTL)
                .build();
    }

    @Bean
    Queue clientEmailCheckQueue() {
        return QueueBuilder.durable("client.email.check")
                .ttl(QUEUE_TTL)
                .build();
    }

    @Bean
    Queue employeeEmailCheckQueue() {
        return QueueBuilder.durable("employee.email.check")
                .ttl(QUEUE_TTL)
                .build();
    }

    @Bean
    Queue clientEmailCheckResponseQueue() {
        return QueueBuilder.durable("client.email.check.response")
                .ttl(QUEUE_TTL)
                .build();
    }

    @Bean
    Queue employeeEmailCheckResponseQueue() {
        return QueueBuilder.durable("employee.email.check.response")
                .ttl(QUEUE_TTL)
                .build();
    }
    /*
     * Email Verification Flow Bindings
     */
    @Bean
    Binding registrationRequestBinding(@Qualifier("registrationRequestQueue") Queue registrationRequestQueue, DirectExchange exchange) {
    	return BindingBuilder.bind(registrationRequestQueue).to(exchange).with("registration.request");
    }
    
    @Bean
    Binding emailVerificationRequestBinding(@Qualifier("emailVerificationRequestQueue") Queue emailVerificationRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(emailVerificationRequestQueue).to(exchange).with("email.verification.request");
    }

    @Bean
    Binding clientEmailCheckBinding(@Qualifier("clientEmailCheckQueue") Queue clientEmailCheckQueue, DirectExchange exchange) {
        return BindingBuilder.bind(clientEmailCheckQueue).to(exchange).with("client.email.check");
    }

    @Bean
    Binding employeeEmailCheckBinding(@Qualifier("employeeEmailCheckQueue") Queue employeeEmailCheckQueue, DirectExchange exchange) {
        return BindingBuilder.bind(employeeEmailCheckQueue).to(exchange).with("employee.email.check");
    }

    @Bean
    Binding clientEmailCheckResponseBinding(@Qualifier("clientEmailCheckResponseQueue") Queue clientEmailCheckResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(clientEmailCheckResponseQueue).to(exchange).with("client.email.check.response");
    }

    @Bean
    Binding employeeEmailCheckResponseBinding(@Qualifier("employeeEmailCheckResponseQueue") Queue employeeEmailCheckResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(employeeEmailCheckResponseQueue).to(exchange).with("employee.email.check.response");
    }
}
