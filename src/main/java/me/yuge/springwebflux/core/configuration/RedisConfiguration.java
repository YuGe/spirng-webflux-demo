package me.yuge.springwebflux.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.yuge.springwebflux.core.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfiguration {
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Configures a {@link ReactiveRedisTemplate} with
     * {@link String} keys and values.
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());
    }

    /**
     * Configures a {@link ReactiveRedisTemplate} with
     * {@link String} keys and a typed {@link Jackson2JsonRedisSerializer}.
     */
    @Bean
    public ReactiveRedisTemplate<String, Session> reactiveRedisSessionTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Session> serializer = new Jackson2JsonRedisSerializer<>(Session.class);
        serializer.setObjectMapper(objectMapper);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Session> builder = RedisSerializationContext
                .newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Session> serializationContext = builder
                .value(serializer).build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
