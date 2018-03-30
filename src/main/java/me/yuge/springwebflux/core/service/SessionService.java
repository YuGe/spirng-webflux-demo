package me.yuge.springwebflux.core.service;

import me.yuge.springwebflux.core.ApplicationProperties;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Duration sessionTimeout;
    private final ReactiveRedisOperations<String, Session> sessionOperations;

    @Autowired
    public SessionService(ApplicationProperties applicationProperties, ReactiveRedisOperations<String, Session> sessionOperations) {
        this.sessionTimeout = Duration.ofDays(applicationProperties.getSession().getTimeout());
        this.sessionOperations = sessionOperations;
    }

    public Mono<Session> get(String id) {
        final String sessionKey = getSessionKey(id);

        return sessionOperations.opsForValue().get(sessionKey).flatMap(
                session -> sessionOperations.expire(sessionKey, sessionTimeout).flatMap(
                        succeeded -> succeeded ? Mono.just(session) : Mono.empty()
                )
        );
    }

    public Mono<Session> create(User user) {
        final String sessionId = newSessionId(user.getId());
        Session session = new Session(sessionId, user.getId(), user.getUsername(), user.getRoles());
        String sessionKey = getSessionKey(session.getId());

        return sessionOperations.opsForValue().set(sessionKey, session).flatMap(
                setSucceeded -> !setSucceeded
                        ? Mono.empty()
                        : sessionOperations.expire(sessionKey, sessionTimeout).flatMap(
                        expireSucceeded -> expireSucceeded ? Mono.just(session) : Mono.empty()
                )
        );
    }

    public Mono<Void> delete(String id) {
        final String sessionKey = getSessionKey(id);

        return sessionOperations.opsForValue().delete(sessionKey).flatMap(
                (s) -> Mono.empty()
        );
    }

    private String newSessionId(String prefix) {
        // make sure the length of rnd hex string is 16
        final long rnd = SECURE_RANDOM.nextLong() | (1L << 62);
        return prefix + Long.toHexString(rnd);
    }

    private String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

}
