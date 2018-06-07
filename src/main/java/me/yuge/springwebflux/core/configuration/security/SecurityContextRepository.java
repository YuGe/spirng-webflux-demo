package me.yuge.springwebflux.core.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {
    private final BasicAuthenticationConverter basicAuthenticationConverter;
    private final BearerAuthenticationConverter bearerAuthenticationConverter;

    @Autowired
    public SecurityContextRepository(BasicAuthenticationConverter basicAuthenticationConverter, BearerAuthenticationConverter bearerAuthenticationConverter) {
        this.basicAuthenticationConverter = basicAuthenticationConverter;
        this.bearerAuthenticationConverter = bearerAuthenticationConverter;
    }

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        // AuthenticationWebFilter will invoke this when authentication success
        return null;
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        return bearerAuthenticationConverter.apply(serverWebExchange)
                .switchIfEmpty(basicAuthenticationConverter.apply(serverWebExchange))
                .map(SecurityContextImpl::new);
    }
}
