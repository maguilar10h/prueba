package com.informacolombia.prueba.infrastructure.event;

import com.informacolombia.prueba.domain.events.DomainEvent;
import com.informacolombia.prueba.domain.events.InventoryReleasedEvent;
import com.informacolombia.prueba.domain.events.InventoryReservedEvent;
import com.informacolombia.prueba.domain.events.OrderCreatedEvent;
import com.informacolombia.prueba.domain.events.OrderStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * EventProcessor - Processes domain events for deferred processing
 * Handles event routing and business logic triggered by events
 */
@Component
public class EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    public void processEvent(DomainEvent event) {
        try {
            logger.info("Processing event: type={}, occurredOn={}", 
                    event.eventType(), event.occurredOn());

            switch (event) {
                case OrderCreatedEvent orderCreated -> handleOrderCreated(orderCreated);
                case OrderStatusChangedEvent statusChanged -> handleOrderStatusChanged(statusChanged);
                case InventoryReservedEvent inventoryReserved -> handleInventoryReserved(inventoryReserved);
                case InventoryReleasedEvent inventoryReleased -> handleInventoryReleased(inventoryReleased);
            }

            logger.info("Event processed successfully: {}", event.eventType());
        } catch (Exception e) {
            logger.error("Error processing event: {}", event.eventType(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    private void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("Handling OrderCreated event for order: {}", event.orderId());
        // Additional business logic for order creation
        // e.g., update analytics, trigger workflows, etc.
    }

    private void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        logger.info("Handling OrderStatusChanged event for order: {}, {} -> {}", 
                event.orderId(), event.oldStatus(), event.newStatus());
        // Additional business logic for status changes
        // e.g., update dashboards, trigger alerts, etc.
    }

    private void handleInventoryReserved(InventoryReservedEvent event) {
        logger.info("Handling InventoryReserved event for order: {}, product: {}, quantity: {}", 
                event.orderId(), event.productId(), event.quantity());
        // Additional business logic for inventory reservation
    }

    private void handleInventoryReleased(InventoryReleasedEvent event) {
        logger.info("Handling InventoryReleased event for order: {}, product: {}, quantity: {}", 
                event.orderId(), event.productId(), event.quantity());
        // Additional business logic for inventory release
    }
}
