package com.informacolombia.prueba.application.ports.outbound;

import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.valueobjects.ProductId;

import java.util.Optional;

/**
 * CacheRepository - Output port for cache operations
 */
public interface CacheRepository {
    /**
     * Gets a product from cache
     */
    Optional<Product> getProduct(ProductId productId);

    /**
     * Puts a product in cache
     */
    void putProduct(Product product);

    /**
     * Removes a product from cache
     */
    void evictProduct(ProductId productId);

    /**
     * Clears all cache
     */
    void clear();
}
