package me.yuge.springwebflux.websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.yuge.springwebflux.websocket.model.Event;
import me.yuge.springwebflux.websocket.model.Payload;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;


public class ChatWebSocketHandler implements WebSocketHandler {
    final private static UnicastProcessor<Event> EVENT_UNICAST_PROCESSOR = UnicastProcessor.create();
    final private static Flux<Event> OUTPUT_EVENTS = Flux.from(EVENT_UNICAST_PROCESSOR.replay(25).autoConnect());
    final private static AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final ObjectMapper mapper;

    public ChatWebSocketHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull WebSocketSession session) {
        WebSocketMessageSubscriber subscriber = new WebSocketMessageSubscriber(EVENT_UNICAST_PROCESSOR);

        session.receive()
                .map(WebSocketMessage::getPayloadAsText).log()
                .map(this::toEvent).log()
                .subscribe(subscriber::onNext, subscriber::onError, subscriber::onComplete);

        return session.send(OUTPUT_EVENTS.map(this::toJSON).map(session::textMessage));
    }

    private Event toEvent(String json) {
        try {
            return mapper.readValue(json, Event.class);
        } catch (IOException e) {
            throw new RuntimeException("Invalid JSON:" + json, e);
        }
    }

    private String toJSON(Event event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Slf4j
    private static class WebSocketMessageSubscriber {
        private UnicastProcessor<Event> eventUnicastProcessor;
        private Event lastReceivedEvent = null;

        WebSocketMessageSubscriber(UnicastProcessor<Event> eventUnicastProcessor) {
            this.eventUnicastProcessor = eventUnicastProcessor;
        }

        void onNext(Event event) {
            event.setId(ID_GENERATOR.addAndGet(1));
            event.setTime(Instant.now());
            lastReceivedEvent = event;
            eventUnicastProcessor.onNext(event);
        }

        void onError(Throwable error) {
            log.error(error.getMessage());
        }

        void onComplete() {
            if (lastReceivedEvent == null) return;

            eventUnicastProcessor.onNext(Event.builder()
                    .type(Event.Type.USER_LEFT)
                    .payload(new Payload(lastReceivedEvent.getUser()))
                    .build()
            );
        }
    }
}
