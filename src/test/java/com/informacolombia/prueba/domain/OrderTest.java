package com.informacolombia.prueba.domain;

import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.entities.OrderItem;
import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Order Domain Tests
 */
class OrderTest {

    @Test
    void shouldCreateOrderWithPendingStatus() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(100)
        );

        OrderItem item = new OrderItem(
                productId,
                Quantity.of(2),
                product.getPrice()
        );

        Order order = new Order(orderId, userId, List.of(item));

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(orderId, order.getId());
        assertEquals(userId, order.getUserId());
        assertFalse(order.getItems().isEmpty());
    }

    @Test
    void shouldChangeStatusToConfirmed() {
        Order order = createTestOrder();
        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldNotAllowInvalidStatusTransition() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        // Cannot change from DELIVERED (final state)
        assertThrows(IllegalStateException.class, () -> order.changeStatus(OrderStatus.PENDING));
    }

    @Test
    void shouldCalculateTotalCorrectly() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();

        Money price = Money.of(10.0, "USD");
        OrderItem item1 = new OrderItem(productId, Quantity.of(2), price);
        OrderItem item2 = new OrderItem(productId, Quantity.of(3), price);

        Order order = new Order(orderId, userId, List.of(item1, item2));

        Money expectedTotal = Money.of(50.0, "USD"); // 2*10 + 3*10
        assertEquals(expectedTotal.getAmount(), order.getTotalAmount().getAmount());
    }

    private Order createTestOrder() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();

        OrderItem item = new OrderItem(
                productId,
                Quantity.of(1),
                Money.of(10.0, "USD")
        );

        return new Order(orderId, userId, List.of(item));
    }
}
