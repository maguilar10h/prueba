package com.informacolombia.prueba.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.informacolombia.prueba.application.ports.outbound.EventPublisher;
import com.informacolombia.prueba.domain.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * KafkaEventPublisher - Kafka implementation of EventPublisher
 * Publishes events asynchronously with retry mechanism
 */
@Component
public class KafkaEventPublisher implements EventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public void publish(DomainEvent event) {
        try {
            String topic = getTopicForEvent(event);
            String message = objectMapper.writeValueAsString(event);
            
            logger.debug("Publishing event to topic {}: {}", topic, event.eventType());
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, event.eventType(), message);
            
            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    logger.error("Failed to publish event {} to topic {}", event.eventType(), topic, exception);
                } else {
                    logger.debug("Successfully published event {} to topic {} at offset {}", 
                            event.eventType(), topic, result.getRecordMetadata().offset());
                }
            });
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event: {}", event.eventType(), e);
            throw new RuntimeException("Failed to serialize event", e);
        } catch (Exception e) {
            logger.error("Failed to publish event: {}", event.eventType(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public void publishAll(Iterable<DomainEvent> events) {
        events.forEach(this::publish);
    }

    private String getTopicForEvent(DomainEvent event) {
        return switch (event.eventType()) {
            case "OrderCreated", "OrderStatusChanged" -> "order-events";
            case "InventoryReserved", "InventoryReleased" -> "inventory-events";
            default -> "notifications";
        };
    }
}
