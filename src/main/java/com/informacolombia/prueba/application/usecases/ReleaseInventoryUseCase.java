package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.domain.entities.Inventory;
import com.informacolombia.prueba.domain.repositories.InventoryRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ReleaseInventoryUseCase - Use case for releasing inventory reservations
 */
@Service
public class ReleaseInventoryUseCase {
    private final InventoryRepository inventoryRepository;

    public ReleaseInventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void execute(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }

        List<Inventory> activeReservations = inventoryRepository.findActiveByOrderId(orderId);
        for (Inventory inventory : activeReservations) {
            inventory.release();
            inventoryRepository.save(inventory);
        }
    }
}
