package com.informacolombia.prueba.domain.events;

import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.UserId;

import java.time.Instant;

/**
 * OrderCreatedEvent - Domain event fired when an order is created
 */
public record OrderCreatedEvent(
        OrderId orderId,
        UserId userId,
        Instant occurredOn
) implements DomainEvent {

    public OrderCreatedEvent(OrderId orderId, UserId userId) {
        this(orderId, userId, Instant.now());
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "OrderCreated";
    }
}
