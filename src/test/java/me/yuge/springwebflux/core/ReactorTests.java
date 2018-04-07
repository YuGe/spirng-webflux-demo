package me.yuge.springwebflux.core;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Random;


public class ReactorTests {

    @Test
    public void testFluxGenerate() {

        Flux.generate(sink -> {
            sink.next("Hello");
            sink.complete();
        }).subscribe(System.out::println);

        final Random random = new Random();
        Flux.generate(ArrayList::new, (list, sink) -> {
            int value = random.nextInt(100);
            list.add(value);
            sink.next(value);
            if (list.size() == 10) {
                sink.complete();
            }
            return list;
        }).subscribe(System.out::println);
    }

    @Test
    public void testFluxCreate() {

        Flux.create(sink -> {
            for (int i = 0; i < 10; i++) {
                sink.next(i);
            }
            sink.complete();
        }).subscribe(System.out::println);

    }

    @Test
    public void testFluxCustomSubscribe() {

        SampleSubscriber<Integer> ss = new SampleSubscriber<>();
        Flux<Integer> ints = Flux.range(1, 20);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"),
                s -> ss.request(10));
        ints.subscribe(ss);

    }

    class SampleSubscriber<T> extends BaseSubscriber<T> {

        public void hookOnSubscribe(Subscription subscription) {
            System.out.println("Subscribed");
            request(1);
        }

        public void hookOnNext(T value) {
            System.out.println("SS" + value);
            request(1);
        }

        public void hookOnComplete() {
            System.out.println("Completed");
        }
    }

}
