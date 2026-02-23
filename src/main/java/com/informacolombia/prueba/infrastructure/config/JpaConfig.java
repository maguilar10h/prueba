package com.informacolombia.prueba.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * JPA Configuration
 * 
 * Configures JPA transaction management with deadlock detection.
 * PostgreSQL automatically detects deadlocks and rolls back one of the transactions.
 * The retry mechanism in use cases will handle deadlock retries.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.informacolombia.prueba.infrastructure.persistence")
@EnableTransactionManagement
public class JpaConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        // Default timeout helps prevent long-running transactions that can cause deadlocks
        transactionManager.setDefaultTimeout(30); // 30 seconds
        return transactionManager;
    }
}
