package com.informacolombia.prueba.domain.events;

import java.time.Instant;

/**
 * DomainEvent - Base sealed interface for all domain events
 * Using sealed interface (Java 17+) allows exhaustive pattern matching in switch expressions (Java 21)
 */
public sealed interface DomainEvent
        permits OrderCreatedEvent, OrderStatusChangedEvent, InventoryReservedEvent, InventoryReleasedEvent {
    /**
     * Returns the timestamp when the event occurred
     */
    Instant occurredOn();

    /**
     * Returns the type of the event
     */
    String eventType();
}
