package com.informacolombia.prueba.domain.repositories;

import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.OrderStatus;
import com.informacolombia.prueba.domain.valueobjects.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * OrderRepository - Output port for order persistence
 */
public interface OrderRepository {
    /**
     * Saves an order
     */
    Order save(Order order);

    /**
     * Finds an order by ID
     */
    Optional<Order> findById(OrderId orderId);

    /**
     * Finds all orders for a user
     */
    List<Order> findByUserId(UserId userId);

    /**
     * Finds orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds orders by user and status
     */
    List<Order> findByUserIdAndStatus(UserId userId, OrderStatus status);

    /**
     * Finds orders created between dates
     */
    List<Order> findByCreatedAtBetween(Instant startDate, Instant endDate);

    /**
     * Deletes an order
     */
    void delete(Order order);
}
