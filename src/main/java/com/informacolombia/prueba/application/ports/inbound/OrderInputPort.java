package com.informacolombia.prueba.application.ports.inbound;

import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * OrderInputPort - Input port for order operations
 * Defines the interface that controllers use to interact with the application
 */
public interface OrderInputPort {
    /**
     * Creates a new order
     */
    Order createOrder(UserId userId, List<OrderItemRequest> items);

    /**
     * Gets an order by ID
     */
    Optional<Order> getOrder(OrderId orderId);

    /**
     * Updates an order
     */
    Order updateOrder(OrderId orderId, OrderUpdateRequest request);

    /**
     * Cancels an order
     */
    Order cancelOrder(OrderId orderId);

    /**
     * Searches orders with filters
     */
    List<Order> searchOrders(OrderSearchRequest request);

    record OrderItemRequest(
            String productId,
            Integer quantity
    ) {}

    record OrderUpdateRequest(
            // Add fields as needed
    ) {}

    record OrderSearchRequest(
            String userId,
            String status,
            String startDate,
            String endDate
    ) {}
}
