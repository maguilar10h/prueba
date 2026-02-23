package com.informacolombia.prueba.domain.repositories;

import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.valueobjects.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository - Output port for product persistence
 */
public interface ProductRepository {
    /**
     * Saves a product
     */
    Product save(Product product);

    /**
     * Finds a product by ID
     */
    Optional<Product> findById(ProductId productId);

    /**
     * Finds all products
     */
    List<Product> findAll();

    /**
     * Deletes a product
     */
    void delete(Product product);
}
