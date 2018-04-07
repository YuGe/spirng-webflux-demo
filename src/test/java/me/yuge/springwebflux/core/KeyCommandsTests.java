package me.yuge.springwebflux.core;

import me.yuge.springwebflux.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveListCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class KeyCommandsTests {

    private static final String PREFIX = KeyCommandsTests.class.getSimpleName();
    private static final String KEY_PATTERN = PREFIX + "*";

    @Autowired
    ReactiveRedisConnectionFactory connectionFactory;

    private ReactiveRedisConnection connection;
    private final RedisSerializer<String> serializer = new StringRedisSerializer();

    @Before
    public void setUp() {
        this.connection = connectionFactory.getReactiveConnection();
    }

    /**
     * Uses {@code KEYS} command for loading all matching keys. <br />
     * Note that {@code KEYS} is a blocking command that potentially might affect other operations execution time. <br />
     * All keys will be loaded within <strong>one single</strong> operation.
     */
    @Test
    public void iterateOverKeysMatchingPrefixUsingKeysCommand() {

        generateRandomKeys();

        Mono<Long> keyCount = connection.keyCommands() //
                .keys(ByteBuffer.wrap(serializer.serialize(KEY_PATTERN))) //
                .flatMapMany(Flux::fromIterable) //
                .doOnNext(byteBuffer -> System.out.println(toString(byteBuffer))) //
                .count() //
                .doOnSuccess(count -> System.out.println(String.format("Total No. found: %s", count)));

        StepVerifier.create(keyCount).expectNext(50L).verifyComplete();
    }

    /**
     * Uses {@code RPUSH} to store an item inside a list and {@code BRPOP} <br />
     */
    @Test
    public void storeToListAndPop() {

        Mono<ReactiveListCommands.PopResult> popResult = connection.listCommands()
                .brPop(Collections.singletonList(ByteBuffer.wrap("list".getBytes())), Duration.ofSeconds(5));

        Mono<Long> llen = connection.listCommands().lLen(ByteBuffer.wrap("list".getBytes()));

        Mono<Long> popAndLlen = connection.listCommands() //
                .rPush(ByteBuffer.wrap("list".getBytes()), Collections.singletonList(ByteBuffer.wrap("item".getBytes())))
                .flatMap(l -> popResult) //
                .doOnNext(result -> System.out.println(toString(result.getValue()))) //
                .flatMap(result -> llen) //
                .doOnNext(count -> System.out.println(String.format("Total items in list left: %s", count)));//

        StepVerifier.create(popAndLlen).expectNext(0L).verifyComplete();
    }

    private void generateRandomKeys() {

        Flux<String> keyFlux = Flux.range(0, 50).map(i -> (PREFIX + "-" + i));

        Flux<ReactiveStringCommands.SetCommand> generator = keyFlux.map(String::getBytes).map(ByteBuffer::wrap) //
                .map(key -> ReactiveStringCommands.SetCommand.set(key) //
                        .value(ByteBuffer.wrap(UUID.randomUUID().toString().getBytes())));

        StepVerifier.create(connection.stringCommands().set(generator))
                .expectNextCount(50)
                .verifyComplete();

    }

    private static String toString(ByteBuffer byteBuffer) {
        return new String(ByteUtils.getBytes(byteBuffer));
    }

}
