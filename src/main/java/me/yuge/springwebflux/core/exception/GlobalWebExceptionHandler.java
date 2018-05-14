package me.yuge.springwebflux.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;


@Slf4j
@Order(-2)
@Component
public class GlobalWebExceptionHandler implements WebExceptionHandler {
    private final ServerAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public GlobalWebExceptionHandler(ServerAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        if (ex instanceof AuthenticationException) {
            return authenticationEntryPoint.commence(exchange, (AuthenticationException) ex);
        }

        // TODO: 2018/03/31  Override DefaultErrorWebExceptionHandler to customize response
        log.warn(ex.getClass().toString());
        log.warn(ex.getMessage());

        return Mono.error(ex);
    }
}
