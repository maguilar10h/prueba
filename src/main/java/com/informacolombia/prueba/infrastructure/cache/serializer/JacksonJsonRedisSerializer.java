package com.informacolombia.prueba.infrastructure.cache.serializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Custom JSON serializer for Redis using Jackson ObjectMapper.
 * 
 * This serializer provides type-safe JSON serialization/deserialization
 * for Redis operations, supporting complex objects with proper type handling.
 * Includes type information in JSON to enable correct deserialization.
 * 
 * @author Informacolombia
 */
public class JacksonJsonRedisSerializer implements RedisSerializer<Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(JacksonJsonRedisSerializer.class);
    private static final byte[] EMPTY_ARRAY = new byte[0];
    
    private final ObjectMapper objectMapper;

    /**
     * Creates a new JacksonJsonRedisSerializer with the provided ObjectMapper.
     * Configures the ObjectMapper to include type information in JSON.
     * 
     * @param objectMapper the ObjectMapper to use for serialization/deserialization
     * @throws IllegalArgumentException if objectMapper is null
     */
    public JacksonJsonRedisSerializer(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new IllegalArgumentException("ObjectMapper cannot be null");
        }
        // Create a copy to avoid modifying the shared ObjectMapper
        this.objectMapper = objectMapper.copy();
        // Enable type information in JSON for proper deserialization
        this.objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
    }

    /**
     * Serializes an object to JSON bytes.
     * 
     * @param obj the object to serialize
     * @return byte array representation of the object, or empty array if obj is null
     * @throws SerializationException if serialization fails
     */
    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            return EMPTY_ARRAY;
        }
        
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            logger.error("Failed to serialize object of type: {}", obj.getClass().getName(), e);
            throw new SerializationException(
                String.format("Error serializing object of type %s", obj.getClass().getName()), 
                e
            );
        }
    }

    /**
     * Deserializes JSON bytes to an object.
     * 
     * @param bytes the byte array to deserialize
     * @return the deserialized object, or null if bytes is null or empty
     * @throws SerializationException if deserialization fails
     */
    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        
        try {
            return objectMapper.readValue(bytes, Object.class);
        } catch (Exception e) {
            String content = new String(bytes, StandardCharsets.UTF_8);
            logger.error("Failed to deserialize JSON content: {}", 
                content.length() > 200 ? content.substring(0, 200) + "..." : content, e);
            throw new SerializationException("Error deserializing JSON content", e);
        }
    }
}
