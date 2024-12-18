package br.ufpr.dac.MSReserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
	    DataSourceAutoConfiguration.class,
	    HibernateJpaAutoConfiguration.class
	})
	@ComponentScan(basePackages = {"br.ufpr.dac.MSReserva",
		    "br.ufpr.dac.MSReserva.cqrs.command",
		    "br.ufpr.dac.MSReserva.cqrs.query"})
	public class MsReservaApplication {
	    public static void main(String[] args) {
	        SpringApplication.run(MsReservaApplication.class, args);
	    }
	}
