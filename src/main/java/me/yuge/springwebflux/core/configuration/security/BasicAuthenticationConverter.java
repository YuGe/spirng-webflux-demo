package me.yuge.springwebflux.core.configuration.security;

import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.service.SessionService;
import me.yuge.springwebflux.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Base64;
import java.util.function.Function;

@Component
public class BasicAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
    private static final String BASIC = "Basic ";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    @Autowired
    public BasicAuthenticationConverter(UserService userService, PasswordEncoder passwordEncoder,
                                        SessionService sessionService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Override
    public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BASIC)) {
            return Mono.empty();
        }

        String credentials = authorization.substring(BASIC.length(), authorization.length());
        String decodedCredentials = new String(base64Decode(credentials));
        String[] loginPassword = decodedCredentials.split(":");
        if (loginPassword.length != 2) {
            return Mono.error(new BadCredentialsException("Basic Credential Format Error"));
        }

        String login = loginPassword[0];
        String password = loginPassword[1];
        return userService.findByLogin(login)
                .publishOn(Schedulers.parallel())
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid Basic Credential"))))
                .flatMap(user -> {
                            final Session session = Session.builder()
                                    .id(Session.nextSessionId(user.getId()))
                                    .userId(user.getId())
                                    .username(user.getUsername())
                                    .roles(user.getRoles())
                                    .login(login)
                                    .build();
                            return sessionService.saveUserSession(session)
                                    .map(savedSession -> new SessionDetailsAuthenticationToken(
                                            savedSession.getId(),
                                            user.getPassword(),
                                            savedSession,
                                            User.getAuthorities(session.getRoles())));
                        }
                );
    }

    private byte[] base64Decode(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
