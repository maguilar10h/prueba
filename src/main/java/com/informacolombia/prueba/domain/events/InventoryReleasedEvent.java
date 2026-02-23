package com.informacolombia.prueba.domain.events;

import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;

import java.time.Instant;

/**
 * InventoryReleasedEvent - Domain event fired when inventory is released
 */
public record InventoryReleasedEvent(
        OrderId orderId,
        ProductId productId,
        Quantity quantity,
        Instant occurredOn
) implements DomainEvent {

    public InventoryReleasedEvent(OrderId orderId, ProductId productId, Quantity quantity) {
        this(orderId, productId, quantity, Instant.now());
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "InventoryReleased";
    }
}
