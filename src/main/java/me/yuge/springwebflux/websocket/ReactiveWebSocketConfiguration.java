package me.yuge.springwebflux.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.yuge.springwebflux.websocket.handler.ChatWebSocketHandler;
import me.yuge.springwebflux.websocket.handler.EchoWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ReactiveWebSocketConfiguration {
    private final ObjectMapper objectMapper;

    @Autowired
    public ReactiveWebSocketConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public HandlerMapping webSocketMapping() {
        Map<String, Object> map = new HashMap<>();
        map.put("/ws/chat", new ChatWebSocketHandler(objectMapper));
        map.put("/ws/echo", new EchoWebSocketHandler());

        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);
        // Without the order things break
        simpleUrlHandlerMapping.setOrder(1);
        return simpleUrlHandlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
