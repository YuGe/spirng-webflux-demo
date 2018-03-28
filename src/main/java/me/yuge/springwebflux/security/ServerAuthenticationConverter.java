package me.yuge.springwebflux.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class ServerAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {

    private final BasicAuthentication basicAuthentication;
    private final BearerAuthentication bearerAuthentication;

    @Autowired
    public ServerAuthenticationConverter(BasicAuthentication basicAuthentication, BearerAuthentication bearerAuthentication) {
        this.basicAuthentication = basicAuthentication;
        this.bearerAuthentication = bearerAuthentication;
    }

    @Override
    public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        //noinspection ConstantConditions
        if (authorization == null) {
            throw new BadCredentialsException("No authorization header");
        }

        if (authorization.startsWith(BearerAuthentication.BEARER)) {
            return bearerAuthentication.apply(authorization);
        }

        if (authorization.startsWith(BasicAuthentication.BASIC)) {
            return basicAuthentication.apply(authorization);
        }

        return Mono.error(new BadCredentialsException("Invalid authorization header"));
    }

}
