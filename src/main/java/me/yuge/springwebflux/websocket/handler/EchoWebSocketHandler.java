package me.yuge.springwebflux.websocket.handler;

import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;


public class EchoWebSocketHandler implements WebSocketHandler {
    @NonNull
    @Override
    public Mono<Void> handle(@NonNull WebSocketSession session) {
        return session.send(session.receive().doOnNext(WebSocketMessage::retain));
    }
}
