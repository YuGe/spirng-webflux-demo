package me.yuge.springwebflux.core.service;

import io.lettuce.core.RedisCommandExecutionException;
import me.yuge.springwebflux.core.ApplicationProperties;
import me.yuge.springwebflux.core.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class SessionService {

    private final String sessionPrefix;
    private final ReactiveRedisOperations<String, Session> sessionOperations;

    @Autowired
    public SessionService(ApplicationProperties applicationProperties,
                          ReactiveRedisOperations<String, Session> sessionOperations) {
        this.sessionPrefix = applicationProperties.getSession().getPrefix();
        this.sessionOperations = sessionOperations;
    }

    public Mono<Session> get(String id) {
        return sessionOperations.opsForValue().get(getSessionKey(id));
    }

    public Mono<Session> save(Session session) {
        return sessionOperations.opsForValue().set(getSessionKey(session.getId()), session).flatMap(
                succeeded -> succeeded
                        ? Mono.just(session)
                        : Mono.error(new RedisCommandExecutionException("set error"))
        );
    }

    public Mono<Session> expire(Session session) {
        return sessionOperations.expire(getSessionKey(session.getId()), session.getMaxIdleTime()).flatMap(
                succeeded -> succeeded
                        ? Mono.just(session)
                        : Mono.error(new RedisCommandExecutionException("expire error"))
        );
    }

    public Mono<Void> delete(String id) {
        return sessionOperations.opsForValue().delete(getSessionKey(id)).flatMap(
                succeeded -> succeeded
                        ? Mono.empty()
                        : Mono.error(new RedisCommandExecutionException("delete error"))
        );
    }

    private String getSessionKey(String sessionId) {
        return sessionPrefix + sessionId;
    }
}
