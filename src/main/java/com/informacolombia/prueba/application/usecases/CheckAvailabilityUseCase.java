package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.domain.repositories.ProductRepository;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;
import org.springframework.stereotype.Service;

/**
 * CheckAvailabilityUseCase - Use case for checking product availability
 */
@Service
public class CheckAvailabilityUseCase {
    private final ProductRepository productRepository;

    public CheckAvailabilityUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean execute(ProductId productId, Quantity requestedQuantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product id cannot be null");
        }
        if (requestedQuantity == null) {
            throw new IllegalArgumentException("Requested quantity cannot be null");
        }

        return productRepository.findById(productId)
                .map(product -> product.hasAvailableQuantity(requestedQuantity))
                .orElse(false);
    }
}
