package me.yuge.springwebflux.core.service;

import me.yuge.springwebflux.core.ApplicationProperties;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class SessionService {

    private static final String SESSION_PREFIX = "session:";

    private Duration sessionTimeout;
    private final ReactiveRedisOperations<String, Session> sessionOperations;

    @Autowired
    public SessionService(ApplicationProperties applicationProperties, ReactiveRedisOperations<String, Session> sessionOperations) {
        this.sessionTimeout = Duration.ofDays(applicationProperties.getSession().getTimeout());
        this.sessionOperations = sessionOperations;
    }

    public Mono<Session> get(String sessionId) {
        String sessionKey = getSessionKey(sessionId);

        return sessionOperations.opsForValue().get(sessionKey).flatMap(
                session -> sessionOperations.expire(sessionKey, sessionTimeout).flatMap(
                        succeeded -> succeeded ? Mono.just(session) : Mono.empty()
                )
        );
    }

    public Mono<Session> create(User user) {
        Session session = new Session(newSessionId(), user.getId(), user.getUsername(), user.getRoles());
        String sessionKey = getSessionKey(session.getId());

        return sessionOperations.opsForValue().set(sessionKey, session).flatMap(
                setSucceeded -> !setSucceeded
                        ? Mono.empty()
                        : sessionOperations.expire(sessionKey, sessionTimeout).flatMap(
                        expireSucceeded -> expireSucceeded ? Mono.just(session) : Mono.empty()
                )
        );
    }

    private String newSessionId() {
        UUID uuid = UUID.randomUUID();
        return Long.toHexString(uuid.getLeastSignificantBits()) +
                Long.toHexString(uuid.getMostSignificantBits());
    }

    private String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }
}
