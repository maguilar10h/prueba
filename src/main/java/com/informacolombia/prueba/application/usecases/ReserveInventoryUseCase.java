package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.domain.entities.Inventory;
import com.informacolombia.prueba.domain.repositories.InventoryRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;
import org.springframework.stereotype.Service;

/**
 * ReserveInventoryUseCase - Use case for reserving inventory
 */
@Service
public class ReserveInventoryUseCase {
    private final InventoryRepository inventoryRepository;

    public ReserveInventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Inventory execute(OrderId orderId, ProductId productId, Quantity quantity) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product id cannot be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }

        Inventory inventory = new Inventory(orderId, productId, quantity);
        return inventoryRepository.save(inventory);
    }
}
