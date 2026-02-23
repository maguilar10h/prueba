package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.application.ports.outbound.EventPublisher;
import com.informacolombia.prueba.application.saga.OrderSaga;
import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.entities.OrderItem;
import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.repositories.OrderRepository;
import com.informacolombia.prueba.domain.repositories.ProductRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import jakarta.persistence.OptimisticLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateOrderUseCase - Use case for creating a new order with concurrent-safe operations
 * Features:
 * - Real-time inventory validation and reservation
 * - Optimistic locking with retry mechanism
 * - Automatic rollback on failures using Saga pattern
 */
@Service
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final EventPublisher eventPublisher;
    private final OrderSaga orderSaga;

    public CreateOrderUseCase(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ReserveInventoryUseCase reserveInventoryUseCase,
            EventPublisher eventPublisher,
            OrderSaga orderSaga
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.reserveInventoryUseCase = reserveInventoryUseCase;
        this.eventPublisher = eventPublisher;
        this.orderSaga = orderSaga;
    }

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, org.springframework.dao.TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 2000)
    )
    public Order execute(UserId userId, List<OrderItemRequest> items) {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        OrderId orderId = OrderId.generate();
        List<Product> products = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        try {
            for (OrderItemRequest item : items) {
                Product product = productRepository.findById(item.productId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.productId()));

                if (!product.hasAvailableQuantity(item.quantity())) {
                    throw new IllegalStateException("Insufficient quantity for product: " + item.productId());
                }

                product.reserveQuantity(item.quantity());
                productRepository.save(product);
                products.add(product);

                orderItems.add(new OrderItem(
                        item.productId(),
                        item.quantity(),
                        product.getPrice()
                ));

                reserveInventoryUseCase.execute(orderId, item.productId(), item.quantity());
            }

            Order order = new Order(orderId, userId, orderItems);
            Order savedOrder = orderRepository.save(order);

            savedOrder.getDomainEvents().forEach(event -> {
                if (event instanceof com.informacolombia.prueba.domain.events.DomainEvent domainEvent) {
                    eventPublisher.publish(domainEvent);
                }
            });
            savedOrder.clearDomainEvents();

            return savedOrder;
        } catch (Exception e) {
            rollbackOrderCreation(orderId, products, items);
            throw e;
        }
    }

    private void rollbackOrderCreation(OrderId orderId, List<Product> products, List<OrderItemRequest> items) {
        try {
            for (int i = 0; i < products.size() && i < items.size(); i++) {
                Product product = products.get(i);
                product.releaseQuantity(items.get(i).quantity());
                productRepository.save(product);
            }
            orderSaga.compensateOrderCreation(
                    orderRepository.findById(orderId).orElse(null)
            );
        } catch (Exception rollbackException) {
            // Log rollback failure but don't throw to avoid masking original exception
        }
    }

    public record OrderItemRequest(
            com.informacolombia.prueba.domain.valueobjects.ProductId productId,
            com.informacolombia.prueba.domain.valueobjects.Quantity quantity
    ) {}
}
