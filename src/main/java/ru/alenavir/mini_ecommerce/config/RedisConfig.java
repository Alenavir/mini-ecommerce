package ru.alenavir.mini_ecommerce.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Value("${redis.ttl.orders}")
    private long ordersTtl;

    @Value("${redis.ttl.last-orders}")
    private long lastOrdersTtl;

    @Value("${redis.ttl.products}")
    private long productsTtl;

    @Value("${redis.ttl.users}")
    private long usersTtl;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(ordersTtl))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.string())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer)
                );

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "orders",     defaultConfig.entryTtl(Duration.ofMinutes(ordersTtl)),
                "lastOrders", defaultConfig.entryTtl(Duration.ofMinutes(lastOrdersTtl)),
                "products",   defaultConfig.entryTtl(Duration.ofMinutes(productsTtl)),
                "users",      defaultConfig.entryTtl(Duration.ofMinutes(usersTtl))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}