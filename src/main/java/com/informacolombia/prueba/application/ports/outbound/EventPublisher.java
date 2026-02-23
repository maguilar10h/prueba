package com.informacolombia.prueba.application.ports.outbound;

import com.informacolombia.prueba.domain.events.DomainEvent;

/**
 * EventPublisher - Output port for publishing domain events
 */
public interface EventPublisher {
    /**
     * Publishes a domain event
     */
    void publish(DomainEvent event);

    /**
     * Publishes multiple domain events
     */
    void publishAll(Iterable<DomainEvent> events);
}
