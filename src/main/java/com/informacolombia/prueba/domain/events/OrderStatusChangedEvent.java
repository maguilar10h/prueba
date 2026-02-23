package com.informacolombia.prueba.domain.events;

import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.OrderStatus;

import java.time.Instant;

/**
 * OrderStatusChangedEvent - Domain event fired when an order status changes
 */
public record OrderStatusChangedEvent(
        OrderId orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Instant occurredOn
) implements DomainEvent {

    public OrderStatusChangedEvent(OrderId orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        this(orderId, oldStatus, newStatus, Instant.now());
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "OrderStatusChanged";
    }
}
