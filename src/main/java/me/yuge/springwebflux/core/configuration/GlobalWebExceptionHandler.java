package me.yuge.springwebflux.core.configuration;

import lombok.extern.slf4j.Slf4j;
import me.yuge.springwebflux.core.security.AuthenticationEntryPoint;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof AuthenticationException) {
            log.warn(ex.getMessage());
            AuthenticationEntryPoint authenticationEntryPoint = new AuthenticationEntryPoint();
            return authenticationEntryPoint.commence(exchange, (AuthenticationException) ex);
        }
        log.error(ex.getMessage());
        return Mono.error(ex);
    }
}
