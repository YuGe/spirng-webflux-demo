package me.yuge.springwebflux.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yuge.springwebflux.core.model.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisOperationsTests {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ReactiveRedisOperations<String, Session> sessionOperations;

    @Test
    public void testWriteReadSession() throws JsonProcessingException {
        Session session = Session.builder().id("id").userId("user_id").username("中文").build();

        StepVerifier.create(sessionOperations.opsForValue().set("session", session))
                .expectNext(true)
                .verifyComplete();

        Flux<String> get = sessionOperations.execute(
                conn -> conn.stringCommands().get(ByteBuffer.wrap("session".getBytes()))
        ).map(ByteUtils::getBytes).map(String::new);

        StepVerifier.create(get)
                .expectNext(objectMapper.writeValueAsString(session))
                .verifyComplete();

        StepVerifier.create(sessionOperations.opsForValue().get("session"))
                .expectNext(session)
                .verifyComplete();

        StepVerifier.create(sessionOperations.opsForValue().delete("session"))
                .expectNext(true)
                .verifyComplete();
    }
}
