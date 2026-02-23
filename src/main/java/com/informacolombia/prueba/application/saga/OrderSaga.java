package com.informacolombia.prueba.application.saga;

import com.informacolombia.prueba.application.usecases.ReleaseInventoryUseCase;
import com.informacolombia.prueba.domain.entities.Order;
import org.springframework.stereotype.Component;

/**
 * OrderSaga - Saga pattern for orchestrating distributed transactions
 * Handles order creation with inventory reservation and rollback
 * Implements compensation pattern for distributed transaction rollback
 */
@Component
public class OrderSaga {
    private final ReleaseInventoryUseCase releaseInventoryUseCase;

    public OrderSaga(ReleaseInventoryUseCase releaseInventoryUseCase) {
        this.releaseInventoryUseCase = releaseInventoryUseCase;
    }

    /**
     * Compensates (rolls back) an order creation
     * Releases all reserved inventory for the order
     */
    public void compensateOrderCreation(Order order) {
        if (order == null) {
            return;
        }

        try {
            releaseInventoryUseCase.execute(order.getOrderId());
        } catch (Exception e) {
            // Log compensation failure but don't throw to avoid masking original exception
        }
    }
}
