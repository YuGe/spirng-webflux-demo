package me.yuge.springwebflux.security;

import me.yuge.springwebflux.model.User;
import me.yuge.springwebflux.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class BearerAuthentication implements Function<String, Mono<Authentication>> {

    static final String BEARER = "Bearer ";

    private final SessionService sessionService;

    @Autowired
    public BearerAuthentication(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Mono<Authentication> apply(String authorization) {
        String token = authorization.substring(7);

        return sessionService.get(token)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid token")))
                .map(session -> {
                    User user = User.builder().roles(session.getRoles()).build();
                    return new AuthenticationToken(session.getUserId(), token, session, user.getAuthorities());
                });
    }
}
