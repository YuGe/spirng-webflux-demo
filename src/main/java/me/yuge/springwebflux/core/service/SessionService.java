package me.yuge.springwebflux.core.service;

import me.yuge.springwebflux.core.configuration.SessionProperties;
import me.yuge.springwebflux.core.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Service
public class SessionService {

    private final String sessionPrefix;
    private final String userSessionsPrefix;
    private final ReactiveRedisOperations<String, String> stringOperations;
    private final ReactiveRedisOperations<String, Session> sessionOperations;

    @Autowired
    public SessionService(SessionProperties sessionProperties,
                          ReactiveRedisOperations<String, String> stringOperations,
                          ReactiveRedisOperations<String, Session> sessionOperations) {
        this.sessionPrefix = sessionProperties.getPrefix();
        this.userSessionsPrefix = sessionProperties.getUserSessionsPrefix();
        this.sessionOperations = sessionOperations;
        this.stringOperations = stringOperations;
    }

    public Mono<Session> getAuthenticatedSession() {
        return ReactiveSecurityContextHolder.getContext().map(
                securityContext -> securityContext.getAuthentication().getDetails()
        ).cast(Session.class);
    }

    public Mono<Session> get(String id) {
        return sessionOperations.opsForValue().get(getSessionKey(id));
    }

    public Mono<Session> save(Session session) {
        Objects.requireNonNull(session.getId());
        return sessionOperations.opsForValue().set(getSessionKey(session.getId()), session)
                .filter(Boolean::booleanValue)
                .map(succeeded -> session);
    }

    public Mono<Session> expire(Session session) {
        Objects.requireNonNull(session.getId());
        return sessionOperations.expire(getSessionKey(session.getId()), session.getMaxIdleTime())
                .filter(Boolean::booleanValue)
                .map(succeeded -> session);
    }

    public Mono<Void> delete(String id) {
        return sessionOperations.opsForValue().delete(getSessionKey(id))
                .filter(Boolean::booleanValue)
                .then();
    }

    public Mono<Session> saveUserSession(Session session) {
        Objects.requireNonNull(session.getUserId());
        return save(session).flatMap(
                savedSession -> stringOperations.opsForList().rightPush(
                        getUserSessionsKey(savedSession.getUserId()), savedSession.getId()
                ).filter(count -> count > 0).map(count -> savedSession)
        );
    }

    public Flux<Session> getUserSessionAll(String userId) {
        return getSavedUserSessionKeyAll(userId).flatMap(
                keys -> sessionOperations.opsForValue().multiGet(keys)
        ).flatMapMany(Flux::fromIterable);
    }

    public Mono<Void> deleteUserSessionAll(String userId) {
        return getSavedUserSessionKeyAll(userId).flatMap(
                keys -> {
                    keys.add(getUserSessionsKey(userId));
                    return sessionOperations.delete(keys.toArray(new String[]{}));
                }
        ).then();
    }

    private Mono<List<String>> getSavedUserSessionKeyAll(String userId) {
        return stringOperations.opsForList().range(
                getUserSessionsKey(userId), 0, -1
        ).map(this::getSessionKey).collectList().filter(keys -> !keys.isEmpty());
    }

    private String getSessionKey(String sessionId) {
        return sessionPrefix + sessionId;
    }

    private String getUserSessionsKey(String userId) {
        return userSessionsPrefix + userId;
    }
}
