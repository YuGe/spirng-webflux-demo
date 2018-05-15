package me.yuge.springwebflux.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.websocket.model.Event;
import me.yuge.springwebflux.websocket.model.Payload;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketHandlerTests {
    @LocalServerPort
    private String port;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void testEcho() throws Exception {
        Duration timeout = Duration.ofMillis(1000);

        int count = 4;
        Flux<String> input = Flux.range(1, count).map(index -> "msg-" + index);
        ReplayProcessor<String> output = ReplayProcessor.create(count);

        WebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(getUrl("/ws/echo"), session -> session
                .send(input.map(session::textMessage))
                .thenMany(session.receive().take(count).map(WebSocketMessage::getPayloadAsText))
                .subscribeWith(output)
                .then()
        ).block(timeout);

        Assert.assertEquals(input.collectList().block(timeout), output.collectList().block(timeout));
    }

    @Test
    public void testChat() throws URISyntaxException {
        Duration timeout = Duration.ofMillis(10000);

        int count = 4;
        Flux<Event> input = Flux.range(1, count).map(index ->
                Event.builder()
                        .type(Event.Type.CHAT_MESSAGE)
                        .payload(new Payload(User.builder().username("Chat1").build()))
                        .build()
        );
        ReplayProcessor<Event> output = ReplayProcessor.create(count);

        WebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(getUrl("/ws/chat"), session -> session
                .send(input.map(this::toJSON).map(session::textMessage))
                .thenMany(session.receive().take(count))
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toEvent)
                .subscribeWith(output)
                .then()
        ).block(timeout);

        Assert.assertEquals(input.map(Event::getPayload).collectList().block(timeout),
                output.map(Event::getPayload).collectList().block(timeout));
    }

    private URI getUrl(String path) throws URISyntaxException {
        return new URI("ws://localhost:" + this.port + path);
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
}
