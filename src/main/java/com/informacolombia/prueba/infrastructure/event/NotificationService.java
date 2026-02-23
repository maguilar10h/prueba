package com.informacolombia.prueba.infrastructure.event;

import com.informacolombia.prueba.domain.events.DomainEvent;
import com.informacolombia.prueba.domain.events.InventoryReleasedEvent;
import com.informacolombia.prueba.domain.events.InventoryReservedEvent;
import com.informacolombia.prueba.domain.events.OrderCreatedEvent;
import com.informacolombia.prueba.domain.events.OrderStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * NotificationService - Service for sending asynchronous notifications
 * Processes events and sends notifications to users
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Async("taskExecutor")
    public CompletableFuture<Void> sendOrderCreatedNotification(OrderCreatedEvent event) {
        try {
            logger.info("Sending notification for order created: OrderId={}, UserId={}", 
                    event.orderId(), event.userId());
            
            // In a real implementation, this would:
            // 1. Get user contact information
            // 2. Send email/SMS/push notification
            // 3. Log notification status
            
            logger.info("Notification sent successfully for order: {}", event.orderId());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Failed to send order created notification for order: {}", 
                    event.orderId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> sendOrderStatusChangedNotification(OrderStatusChangedEvent event) {
        try {
            logger.info("Sending notification for order status change: OrderId={}, Status: {} -> {}", 
                    event.orderId(), event.oldStatus(), event.newStatus());
            
            // In a real implementation, this would:
            // 1. Get user contact information
            // 2. Send email/SMS/push notification with status update
            // 3. Log notification status
            
            logger.info("Notification sent successfully for order status change: {}", event.orderId());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Failed to send order status change notification for order: {}", 
                    event.orderId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public void processEvent(DomainEvent event) {
        try {
            if (event instanceof OrderCreatedEvent orderCreated) {
                sendOrderCreatedNotification(orderCreated);
            } else if (event instanceof OrderStatusChangedEvent statusChanged) {
                sendOrderStatusChangedNotification(statusChanged);
            } else if (event instanceof InventoryReservedEvent) {
                logger.debug("No notification handler for InventoryReserved event");
            } else if (event instanceof InventoryReleasedEvent) {
                logger.debug("No notification handler for InventoryReleased event");
            }
        } catch (Exception e) {
            logger.error("Error processing event: {}", event.eventType(), e);
        }
    }
}
