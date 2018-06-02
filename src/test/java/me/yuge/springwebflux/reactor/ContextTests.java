package me.yuge.springwebflux.reactor;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.util.context.Context;

public class ContextTests {
    @Test
    public void testExpectAccessibleContext() {
        StepVerifier.create(Mono.just(1).map(i -> i + 10),
                StepVerifierOptions.create().withInitialContext(Context.of("foo", "bar")))
                .expectAccessibleContext()
                .contains("foo", "bar")
                .then()
                .expectNext(11)
                .verifyComplete();
    }
}
