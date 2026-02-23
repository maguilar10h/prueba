package com.informacolombia.prueba.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Retry Configuration for handling transient failures
 * Handles:
 * - OptimisticLockException (concurrent modifications)
 * - Deadlocks (PostgreSQL error code 40001)
 * - Transient database errors
 */
@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry policy - retry up to 3 times
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Backoff policy (exponential backoff)
        // Helps prevent thundering herd and reduces deadlock probability
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(100); // 100ms initial delay
        backOffPolicy.setMultiplier(2.0); // Double the delay each retry
        backOffPolicy.setMaxInterval(2000); // Max 2 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
