package me.yuge.springwebflux.core.configuration.security;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Primary
@Component
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private static final String DEFAULT_REALM = "Realm";

    private String basicHeaderValue = createBasicHeaderValue(DEFAULT_REALM);
    private String bearerHeaderValue = createBearerHeaderValue(DEFAULT_REALM);

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, this.basicHeaderValue);
        response.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, this.bearerHeaderValue);

        final String message = e instanceof AuthenticationCredentialsNotFoundException
                ? "Authentication Credential Not Found."
                : e.getMessage();

        final DataBuffer buffer = response.bufferFactory().wrap(message.getBytes());
        return response.writeWith(Mono.just(buffer)).doOnError(error -> DataBufferUtils.release(buffer));
    }

    /**
     * Sets the realm to be used
     *
     * @param realm the realm. Default is "Realm"
     */
    @SuppressWarnings("unused")
    public void setRealm(String realm) {
        this.basicHeaderValue = createBasicHeaderValue(realm);
        this.bearerHeaderValue = createBearerHeaderValue(realm);
    }

    private static String createBasicHeaderValue(String realm) {
        Assert.notNull(realm, "realm cannot be null");
        return String.format("Basic realm=\"%s\"", realm);
    }

    private static String createBearerHeaderValue(String realm) {
        Assert.notNull(realm, "realm cannot be null");
        return String.format("Bearer realm=\"%s\"", realm);
    }
}
