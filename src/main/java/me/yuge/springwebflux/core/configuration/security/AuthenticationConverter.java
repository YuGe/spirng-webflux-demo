package me.yuge.springwebflux.core.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;


@Component
public class AuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
    private final BasicAuthentication basicAuthentication;
    private final BearerAuthentication bearerAuthentication;

    @Autowired
    public AuthenticationConverter(BasicAuthentication basicAuthentication, BearerAuthentication bearerAuthentication) {
        this.basicAuthentication = basicAuthentication;
        this.bearerAuthentication = bearerAuthentication;
    }

    @Override
    public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // SecurityContext already exist. For test with mock user.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return Mono.just(SecurityContextHolder.getContext().getAuthentication());
        }
        if (authorization == null) {
            return Mono.empty();
        }
        if (authorization.startsWith(BearerAuthentication.BEARER)) {
            return bearerAuthentication.apply(authorization);
        }
        if (authorization.startsWith(BasicAuthentication.BASIC)) {
            return basicAuthentication.apply(authorization);
        }

        return Mono.error(new BadCredentialsException("Invalid Authorization Header"));
    }
}
