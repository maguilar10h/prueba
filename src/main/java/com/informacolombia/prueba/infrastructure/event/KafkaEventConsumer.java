package com.informacolombia.prueba.infrastructure.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.informacolombia.prueba.domain.events.DomainEvent;
import com.informacolombia.prueba.domain.events.InventoryReleasedEvent;
import com.informacolombia.prueba.domain.events.InventoryReservedEvent;
import com.informacolombia.prueba.domain.events.OrderCreatedEvent;
import com.informacolombia.prueba.domain.events.OrderStatusChangedEvent;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.OrderStatus;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * KafkaEventConsumer - Consumes events from Kafka with retry and error handling
 * Processes events asynchronously and sends notifications
 */
@Component
public class KafkaEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final EventProcessor eventProcessor;

    public KafkaEventConsumer(
            ObjectMapper objectMapper,
            NotificationService notificationService,
            EventProcessor eventProcessor
    ) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(
            topics = "order-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void consumeOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received order event from topic {}: {}", topic, message);
            
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            
            DomainEvent event = deserializeOrderEvent(jsonNode, eventType);
            
            if (event != null) {
                eventProcessor.processEvent(event);
                notificationService.processEvent(event);
                logger.info("Successfully processed order event: {}", eventType);
            }
            
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            logger.error("Error processing order event: {}", message, e);
            throw new RuntimeException("Failed to process order event", e);
        }
    }

    @KafkaListener(
            topics = "inventory-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void consumeInventoryEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received inventory event from topic {}: {}", topic, message);
            
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            
            DomainEvent event = deserializeInventoryEvent(jsonNode, eventType);
            
            if (event != null) {
                eventProcessor.processEvent(event);
                logger.info("Successfully processed inventory event: {}", eventType);
            }
            
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            logger.error("Error processing inventory event: {}", message, e);
            throw new RuntimeException("Failed to process inventory event", e);
        }
    }

    @KafkaListener(
            topics = "notifications",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received notification from topic {}: {}", topic, message);
            
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            logger.error("Error processing notification: {}", message, e);
        }
    }

    private DomainEvent deserializeOrderEvent(JsonNode jsonNode, String eventType) {
        try {
            return switch (eventType) {
                case "OrderCreated" -> {
                    OrderId orderId = OrderId.of(jsonNode.get("orderId").get("value").asText());
                    UserId userId = UserId.of(jsonNode.get("userId").get("value").asText());
                    Instant occurredOn = Instant.parse(jsonNode.get("occurredOn").asText());
                    yield new OrderCreatedEvent(orderId, userId, occurredOn);
                }
                case "OrderStatusChanged" -> {
                    OrderId orderId = OrderId.of(jsonNode.get("orderId").get("value").asText());
                    OrderStatus oldStatus = OrderStatus.valueOf(jsonNode.get("oldStatus").asText());
                    OrderStatus newStatus = OrderStatus.valueOf(jsonNode.get("newStatus").asText());
                    Instant occurredOn = Instant.parse(jsonNode.get("occurredOn").asText());
                    yield new OrderStatusChangedEvent(orderId, oldStatus, newStatus, occurredOn);
                }
                default -> null;
            };
        } catch (Exception e) {
            logger.error("Error deserializing order event: {}", eventType, e);
            return null;
        }
    }

    private DomainEvent deserializeInventoryEvent(JsonNode jsonNode, String eventType) {
        try {
            return switch (eventType) {
                case "InventoryReserved" -> {
                    OrderId orderId = OrderId.of(jsonNode.get("orderId").get("value").asText());
                    ProductId productId = ProductId.of(jsonNode.get("productId").get("value").asText());
                    Quantity quantity = Quantity.of(jsonNode.get("quantity").get("value").asInt());
                    Instant occurredOn = Instant.parse(jsonNode.get("occurredOn").asText());
                    yield new InventoryReservedEvent(orderId, productId, quantity, occurredOn);
                }
                case "InventoryReleased" -> {
                    OrderId orderId = OrderId.of(jsonNode.get("orderId").get("value").asText());
                    ProductId productId = ProductId.of(jsonNode.get("productId").get("value").asText());
                    Quantity quantity = Quantity.of(jsonNode.get("quantity").get("value").asInt());
                    Instant occurredOn = Instant.parse(jsonNode.get("occurredOn").asText());
                    yield new InventoryReleasedEvent(orderId, productId, quantity, occurredOn);
                }
                default -> null;
            };
        } catch (Exception e) {
            logger.error("Error deserializing inventory event: {}", eventType, e);
            return null;
        }
    }
}
