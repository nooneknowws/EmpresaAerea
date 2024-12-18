package br.ufpr.dac.SAGA.queues;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SAGAExchange {
		/*
	    * SAGA QUEUE
	    */
	
	    @Bean
	    DirectExchange exchange() {
	        return new DirectExchange("saga-exchange");
	    }
}
