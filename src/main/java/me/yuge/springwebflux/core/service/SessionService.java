package me.yuge.springwebflux.core.service;

import io.lettuce.core.RedisCommandExecutionException;
import me.yuge.springwebflux.core.ApplicationProperties;
import me.yuge.springwebflux.core.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;


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
        Objects.requireNonNull(session.getId());
        return sessionOperations.opsForValue().set(getSessionKey(session.getId()), session)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new RedisCommandExecutionException("set error")))
                .map(succeeded -> session);
    }

    public Mono<Session> expire(Session session) {
        return sessionOperations.expire(getSessionKey(session.getId()), session.getMaxIdleTime())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new RedisCommandExecutionException("expire error")))
                .map(succeeded -> session);
    }

    public Mono<Void> delete(String id) {
        return sessionOperations.opsForValue().delete(getSessionKey(id))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new RedisCommandExecutionException("delete error")))
                .then();
    }

    public Mono<Session> saveUserSession(Session session) {
        Objects.requireNonNull(session.getId());
        Objects.requireNonNull(session.getUserId());
        return save(session).flatMap(
                savedSession -> sessionOperations.execute(
                        connection -> connection.listCommands().rPush(
                                ByteBuffer.wrap(getUserSessionsKey(savedSession.getUserId()).getBytes()),
                                Collections.singletonList(ByteBuffer.wrap(session.getId().getBytes()))
                        )
                ).count().filter(count -> count > 0)
                        .switchIfEmpty(Mono.error(new RedisCommandExecutionException("rPush error")))
                        .map(count -> savedSession)
        );
    }

    public Mono<Void> deleteUserSessionAll(String userId) {
        return sessionOperations.execute(
                connection -> connection.listCommands().lRange(
                        ByteBuffer.wrap(getUserSessionsKey(userId).getBytes()), 0, -1
                )
        ).map(
                s -> ByteBuffer.wrap(getSessionKey(new String(ByteUtils.getBytes(s))).getBytes())
        ).collectList().flatMap(
                keys -> {
                    keys.add(ByteBuffer.wrap(getUserSessionsKey(userId).getBytes()));
                    return sessionOperations.execute(
                            connection -> connection.keyCommands().mDel(keys)
                    ).then();
                }
        );
    }

    private String getSessionKey(String sessionId) {
        return sessionPrefix + sessionId;
    }

    private String getUserSessionsKey(String userId) {
        return "sessions:" + userId;
    }
}
