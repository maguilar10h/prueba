package com.informacolombia.prueba.infrastructure.cache;

import com.informacolombia.prueba.application.ports.outbound.CacheRepository;
import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * RedisInventoryCache - Redis implementation of CacheRepository
 * Uses Cache-Aside pattern
 */
@Repository
public class RedisInventoryCache implements CacheRepository {
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisInventoryCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Product> getProduct(ProductId productId) {
        String key = getProductKey(productId);
        Product product = (Product) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(product);
    }

    @Override
    public void putProduct(Product product) {
        String key = getProductKey(product.getProductId());
        redisTemplate.opsForValue().set(key, product, TTL);
    }

    @Override
    public void evictProduct(ProductId productId) {
        String key = getProductKey(productId);
        redisTemplate.delete(key);
    }

    @Override
    public void clear() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    private String getProductKey(ProductId productId) {
        return PRODUCT_KEY_PREFIX + productId.getValue();
    }
}
