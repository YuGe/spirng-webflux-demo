package me.yuge.springwebflux.core.configuration.security;

import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class BearerAuthenticationConverter implements Function<String, Mono<Authentication>> {
    static final String BEARER = "Bearer ";
    private final SessionService sessionService;

    @Autowired
    public BearerAuthenticationConverter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Mono<Authentication> apply(String authorization) {
        final String token = authorization.substring(BEARER.length());

        return sessionService.get(token).switchIfEmpty(
                Mono.error(new BadCredentialsException("Invalid Bearer Token"))
        ).flatMap(session -> sessionService.expire(session).map(
                expiredSession -> new SessionDetailsAuthenticationToken(
                        expiredSession.getUserId(),
                        token,
                        expiredSession,
                        User.getAuthorities(expiredSession.getRoles())
                )
        ));
    }
}