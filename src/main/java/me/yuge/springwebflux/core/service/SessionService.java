package me.yuge.springwebflux.core.service;

import io.lettuce.core.RedisCommandExecutionException;
import me.yuge.springwebflux.core.ApplicationProperties;
import me.yuge.springwebflux.core.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Service
public class SessionService {

    private final String sessionPrefix;
    private final Duration sessionTimeout;
    private final ReactiveRedisOperations<String, Session> sessionOperations;

    @Autowired
    public SessionService(ApplicationProperties applicationProperties, ReactiveRedisOperations<String, Session> sessionOperations) {
        this.sessionPrefix = applicationProperties.getSession().getPrefix();
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

    public Mono<Session> save(Session session) {
        String sessionKey = getSessionKey(session.getId());

        return sessionOperations.opsForValue().set(sessionKey, session).flatMap(
                setSucceeded -> !setSucceeded
                        ? Mono.error(new RedisCommandExecutionException("set error"))
                        : sessionOperations.expire(sessionKey, sessionTimeout).flatMap(
                        expireSucceeded -> !expireSucceeded
                                ? Mono.error(new RedisCommandExecutionException("expire error"))
                                : Mono.just(session)
                )
        );
    }

    public Mono<Void> delete(String id) {
        final String sessionKey = getSessionKey(id);

        return sessionOperations.opsForValue().delete(sessionKey).flatMap(
                (s) -> Mono.empty()
        );
    }

    private String getSessionKey(String sessionId) {
        return sessionPrefix + sessionId;
    }
}
