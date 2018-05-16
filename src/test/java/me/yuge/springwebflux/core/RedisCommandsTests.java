package me.yuge.springwebflux.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveListCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisCommandsTests {
    private static final String PREFIX = RedisCommandsTests.class.getSimpleName();
    private static final String KEY_PATTERN = PREFIX + "*";

    @Autowired
    private ReactiveRedisConnectionFactory connectionFactory;

    private ReactiveRedisConnection connection;

    @Before
    public void setUp() {
        this.connection = connectionFactory.getReactiveConnection();
    }

    /**
     * Uses {@code KEYS} command for loading all matching keys. <br/>
     * Note that {@code KEYS} is a blocking command that potentially might affect other operations execution time. <br/>
     * All keys will be loaded within <strong>one single</strong> operation.
     */
    @Test
    public void testIterateOverKeysMatchingPrefixUsingKeysCommand() {
        Mono<Long> setCount = connection.stringCommands().set(Flux.range(0, 10)
                .map(i -> (PREFIX + "-" + i))
                .map(String::getBytes).map(ByteBuffer::wrap)
                .map(key -> ReactiveStringCommands.SetCommand.set(key).value(key))
        ).count();
        StepVerifier.create(setCount).expectNext(10L).verifyComplete();

        Mono<Long> keysCount = connection.keyCommands().del(
                connection.keyCommands()
                        .keys(ByteBuffer.wrap(KEY_PATTERN.getBytes()))
                        .flatMapMany(Flux::fromIterable)
                        .map(ReactiveRedisConnection.KeyCommand::new)
        ).count();
        StepVerifier.create(keysCount).expectNext(10L).verifyComplete();
    }

    /**
     * Uses {@code RPUSH} to store an item inside a list and {@code BRPOP} <br/>
     */
    @Test
    public void testPushToListAndPop() {
        ByteBuffer key = ByteBuffer.wrap("list".getBytes());

        Mono<String> brPop = connection.listCommands()
                .brPop(Collections.singletonList(key), Duration.ofSeconds(5))
                .map(ReactiveListCommands.PopResult::getValue)
                .map(ByteUtils::getBytes)
                .map(String::new);

        Mono<Long> lLen = connection.listCommands().lLen(key);

        Mono<Long> popAndLLen = connection.listCommands()
                .rPush(key, Collections.singletonList(ByteBuffer.wrap("item".getBytes())))
                .flatMap(l -> brPop)
                .doOnNext(i -> Assert.assertEquals("item", i))
                .flatMap(r -> lLen);
        StepVerifier.create(popAndLLen).expectNext(1L).verifyComplete();
    }
}
