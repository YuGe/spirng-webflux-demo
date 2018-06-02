package me.yuge.springwebflux.reactor;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

public class PublisherProbeTests {
    private Mono<String> executeCommand(String command) {
        return Mono.just(command + " DONE");
    }

    private Mono<Void> processOrFallback(Mono<String> commandSource, Mono<Void> doWhenEmpty) {
        return commandSource
                .flatMap(command -> executeCommand(command).then())
                .switchIfEmpty(doWhenEmpty);
    }

    @Test
    public void testPublishProbe() {
        PublisherProbe<Void> probe = PublisherProbe.empty();

        StepVerifier.create(processOrFallback(Mono.empty(), probe.mono()))
                .verifyComplete();
        probe.assertWasSubscribed();
        probe.assertWasRequested();
        probe.assertWasNotCancelled();
    }
}
