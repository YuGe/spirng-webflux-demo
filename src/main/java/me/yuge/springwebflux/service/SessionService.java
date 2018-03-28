package me.yuge.springwebflux.service;

import me.yuge.springwebflux.model.Session;
import me.yuge.springwebflux.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class SessionService {
    private static final String SESSION_PREFIX = "session:";

    private final ReactiveRedisOperations<String, Session> sessionOperations;

    @Autowired
    public SessionService(ReactiveRedisOperations<String, Session> sessionOperations) {
        this.sessionOperations = sessionOperations;
    }

    public Mono<Session> get(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;

        return sessionOperations.opsForValue().get(sessionKey).flatMap(
                session -> sessionOperations.expire(sessionKey, Duration.ofDays(7)).flatMap(
                        succeeded -> succeeded ? Mono.just(session) : Mono.empty()
                )
        );
    }

    public Mono<Session> create(User user) {
        Session session = new Session(newSessionId(), user.getId(), user.getUsername(), user.getRoles());
        String sessionId = SESSION_PREFIX + session.getId();

        return sessionOperations.opsForValue().set(sessionId, session).flatMap(
                (succeeded) -> succeeded ? Mono.just(session) : Mono.empty()
        );
    }

    private String newSessionId() {
        UUID uuid = UUID.randomUUID();
        return Long.toHexString(uuid.getLeastSignificantBits()) +
                Long.toHexString(uuid.getMostSignificantBits());
    }
}
