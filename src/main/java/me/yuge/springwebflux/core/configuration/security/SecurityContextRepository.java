package me.yuge.springwebflux.core.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
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
        ServerHttpRequest request = serverWebExchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
            return Mono.empty();
        }
        if (authorization.startsWith(BearerAuthenticationConverter.BEARER)) {
            return bearerAuthenticationConverter.apply(authorization).map(SecurityContextImpl::new);
        }
        if (authorization.startsWith(BasicAuthenticationConverter.BASIC)) {
            return basicAuthenticationConverter.apply(authorization).map(SecurityContextImpl::new);
        }
        return Mono.error(new BadCredentialsException("Invalid Authorization Header"));
    }
}
