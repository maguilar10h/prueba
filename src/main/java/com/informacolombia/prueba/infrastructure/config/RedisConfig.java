package com.informacolombia.prueba.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.informacolombia.prueba.infrastructure.cache.serializer.JacksonJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 * 
 * Configures RedisTemplate with appropriate serializers for keys and values.
 * Keys are serialized as strings, while values use JSON serialization via Jackson.
 * 
 * @author Informacolombia
 */
@Configuration
public class RedisConfig {

    /**
     * Creates and configures a RedisTemplate for String-Object operations.
     * 
     * Configuration:
     * - Keys: Serialized as UTF-8 strings
     * - Values: Serialized as JSON using Jackson ObjectMapper
     * - Hash keys: Serialized as UTF-8 strings
     * - Hash values: Serialized as JSON using Jackson ObjectMapper
     * 
     * @param connectionFactory the Redis connection factory
     * @param objectMapper the Jackson ObjectMapper for JSON serialization
     * @return configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure key serializers (always strings for Redis keys)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Configure value serializers (JSON for complex objects)
        JacksonJsonRedisSerializer jsonSerializer = new JacksonJsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        // Enable default serialization for null values
        template.setDefaultSerializer(stringSerializer);
        
        // Initialize the template
        template.afterPropertiesSet();
        
        return template;
    }
}
