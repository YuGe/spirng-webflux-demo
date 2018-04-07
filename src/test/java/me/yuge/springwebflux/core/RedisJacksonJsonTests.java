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
public class RedisJacksonJsonTests {

    @Autowired
    ReactiveRedisOperations<String, Session> typedOperations;

    @Autowired
    ReactiveRedisOperations<String, Object> genericOperations;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void shouldWriteAndReadPerson() throws JsonProcessingException {
        Session session = new Session("id", "user_id", "username", new String[]{"USER", "ADMIN"});

        StepVerifier.create(typedOperations.opsForValue().set("session", session))
                .expectNext(true)
                .verifyComplete();

        Flux<String> get = typedOperations.execute(conn -> conn.stringCommands().get(ByteBuffer.wrap("session".getBytes())))
                .doOnNext(System.out::println)
                .map(ByteUtils::getBytes)
                .map(String::new);

        StepVerifier.create(get)
                .expectNext(objectMapper.writeValueAsString(session))
                .verifyComplete();

        StepVerifier.create(typedOperations.opsForValue().get("session"))
                .expectNext(session)
                .verifyComplete();
    }

    @Test
    public void shouldWriteAndReadSessionObject() throws JsonProcessingException {

        Session session = new Session("id", "user_id", "username", new String[]{"USER", "ADMIN"});

        StepVerifier.create(genericOperations.opsForValue().set("session", session))
                .expectNext(true)
                .verifyComplete();

        Flux<String> get = genericOperations.execute(conn -> conn.stringCommands().get(ByteBuffer.wrap("session".getBytes())))
                .map(ByteUtils::getBytes)
                .map(String::new);

        StepVerifier.create(get)
                .expectNext(objectMapper.writeValueAsString(session))
                .verifyComplete();

//        StepVerifier.create(genericOperations.opsForValue().get("mail"))
//                .expectNext(email)
//                .verifyComplete();
    }

}
