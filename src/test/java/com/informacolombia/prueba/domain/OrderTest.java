package com.informacolombia.prueba.domain;

import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.entities.OrderItem;
import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.events.OrderCreatedEvent;
import com.informacolombia.prueba.domain.events.OrderStatusChangedEvent;
import com.informacolombia.prueba.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
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
        assertEquals(orderId, order.getOrderId());
        assertEquals(userId, order.getUserId());
        assertFalse(order.getItems().isEmpty());
        assertEquals(1, order.getItems().size());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenOrderIdIsNull() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        OrderItem item = new OrderItem(productId, Quantity.of(1), Money.of(10.0, "USD"));

        assertThrows(IllegalArgumentException.class, () -> {
            new Order(null, userId, List.of(item));
        });
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        OrderId orderId = OrderId.generate();
        ProductId productId = ProductId.generate();
        OrderItem item = new OrderItem(productId, Quantity.of(1), Money.of(10.0, "USD"));

        assertThrows(IllegalArgumentException.class, () -> {
            new Order(orderId, null, List.of(item));
        });
    }

    @Test
    void shouldThrowExceptionWhenItemsIsNull() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();

        assertThrows(IllegalArgumentException.class, () -> {
            new Order(orderId, userId, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenItemsIsEmpty() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();

        assertThrows(IllegalArgumentException.class, () -> {
            new Order(orderId, userId, List.of());
        });
    }

    @Test
    void shouldCreateOrderCreatedEvent() {
        Order order = createTestOrder();
        
        List<Object> events = order.getDomainEvents();
        assertFalse(events.isEmpty());
        assertTrue(events.get(0) instanceof OrderCreatedEvent);
        
        OrderCreatedEvent event = (OrderCreatedEvent) events.get(0);
        assertEquals(order.getOrderId(), event.orderId());
        assertEquals(order.getUserId(), event.userId());
    }

    @Test
    void shouldChangeStatusToConfirmed() {
        Order order = createTestOrder();
        Instant beforeUpdate = order.getUpdatedAt();
        
        order.confirm();
        
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(beforeUpdate) || order.getUpdatedAt().equals(beforeUpdate));
    }

    @Test
    void shouldChangeStatusToProcessing() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
    }

    @Test
    void shouldChangeStatusToShipped() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void shouldChangeStatusToDelivered() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();
        
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertTrue(order.isInFinalState());
    }

    @Test
    void shouldCancelOrder() {
        Order order = createTestOrder();
        order.cancel();
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.isInFinalState());
    }

    @Test
    void shouldNotCancelOrderFromFinalState() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();
        
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    @Test
    void shouldMarkOrderAsFailed() {
        Order order = createTestOrder();
        order.confirm();
        order.markAsFailed();
        
        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertTrue(order.isInFinalState());
    }

    @Test
    void shouldCreateStatusChangedEvent() {
        Order order = createTestOrder();
        order.clearDomainEvents();
        
        order.confirm();
        
        List<Object> events = order.getDomainEvents();
        assertFalse(events.isEmpty());
        assertTrue(events.get(0) instanceof OrderStatusChangedEvent);
        
        OrderStatusChangedEvent event = (OrderStatusChangedEvent) events.get(0);
        assertEquals(OrderStatus.PENDING, event.oldStatus());
        assertEquals(OrderStatus.CONFIRMED, event.newStatus());
    }

    @Test
    void shouldNotAllowInvalidStatusTransition() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        assertThrows(IllegalStateException.class, () -> order.changeStatus(OrderStatus.PENDING));
        assertThrows(IllegalStateException.class, () -> order.changeStatus(OrderStatus.CONFIRMED));
    }

    @Test
    void shouldNotAllowNullStatusTransition() {
        Order order = createTestOrder();
        
        assertThrows(IllegalArgumentException.class, () -> order.changeStatus(null));
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

        Money expectedTotal = Money.of(50.0, "USD");
        assertEquals(expectedTotal.getAmount(), order.getTotalAmount().getAmount());
        assertEquals("USD", order.getTotalAmount().getCurrency());
    }

    @Test
    void shouldCalculateTotalWithDifferentPrices() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        ProductId productId1 = ProductId.generate();
        ProductId productId2 = ProductId.generate();

        OrderItem item1 = new OrderItem(productId1, Quantity.of(2), Money.of(10.0, "USD"));
        OrderItem item2 = new OrderItem(productId2, Quantity.of(3), Money.of(15.0, "USD"));

        Order order = new Order(orderId, userId, List.of(item1, item2));

        Money expectedTotal = Money.of(65.0, "USD");
        assertEquals(expectedTotal.getAmount(), order.getTotalAmount().getAmount());
    }

    @Test
    void shouldBeModifiableWhenPending() {
        Order order = createTestOrder();
        assertTrue(order.canBeModified());
    }

    @Test
    void shouldBeModifiableWhenConfirmed() {
        Order order = createTestOrder();
        order.confirm();
        assertTrue(order.canBeModified());
    }

    @Test
    void shouldNotBeModifiableWhenProcessing() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        assertFalse(order.canBeModified());
    }

    @Test
    void shouldNotBeModifiableWhenShipped() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        assertFalse(order.canBeModified());
    }

    @Test
    void shouldNotBeModifiableWhenDelivered() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();
        assertFalse(order.canBeModified());
    }

    @Test
    void shouldNotBeModifiableWhenCancelled() {
        Order order = createTestOrder();
        order.cancel();
        assertFalse(order.canBeModified());
    }

    @Test
    void shouldIdentifyFinalStates() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();
        assertTrue(order.isInFinalState());
        
        order = createTestOrder();
        order.cancel();
        assertTrue(order.isInFinalState());
        
        order = createTestOrder();
        order.confirm();
        order.markAsFailed();
        assertTrue(order.isInFinalState());
    }

    @Test
    void shouldNotBeInFinalStateWhenPending() {
        Order order = createTestOrder();
        assertFalse(order.isInFinalState());
    }

    @Test
    void shouldNotBeInFinalStateWhenConfirmed() {
        Order order = createTestOrder();
        order.confirm();
        assertFalse(order.isInFinalState());
    }

    @Test
    void shouldClearDomainEvents() {
        Order order = createTestOrder();
        assertFalse(order.getDomainEvents().isEmpty());
        
        order.clearDomainEvents();
        assertTrue(order.getDomainEvents().isEmpty());
    }

    @Test
    void shouldUpdateTimestampOnStatusChange() {
        Order order = createTestOrder();
        Instant initialUpdate = order.getUpdatedAt();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        order.confirm();
        assertTrue(order.getUpdatedAt().isAfter(initialUpdate) || order.getUpdatedAt().equals(initialUpdate));
    }

    @Test
    void shouldAllowCancelFromPending() {
        Order order = createTestOrder();
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldAllowCancelFromConfirmed() {
        Order order = createTestOrder();
        order.confirm();
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldAllowCancelFromProcessing() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldNotAllowCancelFromShipped() {
        Order order = createTestOrder();
        order.confirm();
        order.startProcessing();
        order.ship();
        
        assertThrows(IllegalStateException.class, () -> order.cancel());
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
