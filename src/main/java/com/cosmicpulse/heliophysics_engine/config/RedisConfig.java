package com.cosmicpulse.heliophysics_engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.cosmicpulse.heliophysics_engine.model.TechHealthScore;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public ReactiveRedisTemplate<String, TechHealthScore> techHealthRedisTemplate(
        ReactiveRedisConnectionFactory factory,
        ObjectMapper objectMapper
    ) {
        var serializer = new Jackson2JsonRedisSerializer<>(objectMapper, TechHealthScore.class);
        var context = RedisSerializationContext.<String, TechHealthScore>newSerializationContext(new StringRedisSerializer())
            .value(serializer)
            .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
