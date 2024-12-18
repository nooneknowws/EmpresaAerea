package br.ufpr.dac.MSReserva.cqrs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
public class RepositoryConfig {

    @Configuration
    @EnableJpaRepositories(
        basePackages = "br.ufpr.dac.reserva.cqrs.command",
        entityManagerFactoryRef = "writeEntityManagerFactory",
        transactionManagerRef = "writeTransactionManager"
    )
    public class WriteRepositoryConfig {
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = "br.ufpr.dac.reserva.cqrs.query",
        entityManagerFactoryRef = "readEntityManagerFactory",
        transactionManagerRef = "readTransactionManager"
    )
    public class ReadRepositoryConfig {
    }
}