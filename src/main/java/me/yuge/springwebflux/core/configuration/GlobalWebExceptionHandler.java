package me.yuge.springwebflux.core.configuration;

import me.yuge.springwebflux.core.security.HttpServerAuthenticationEntryPoint;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof AuthenticationException) {
            HttpServerAuthenticationEntryPoint httpServerAuthenticationEntryPoint = new HttpServerAuthenticationEntryPoint();
            return httpServerAuthenticationEntryPoint.commence(exchange, (AuthenticationException) ex);
        }
        return Mono.error(ex);
    }
}
