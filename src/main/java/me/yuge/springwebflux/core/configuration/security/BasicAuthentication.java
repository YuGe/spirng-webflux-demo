package me.yuge.springwebflux.core.configuration.security;

import me.yuge.springwebflux.core.configuration.SessionProperties;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.service.SessionService;
import me.yuge.springwebflux.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.function.Function;


@Component
public class BasicAuthentication implements Function<String, Mono<Authentication>> {
    static final String BASIC = "Basic ";

    private final UserService userService;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final Duration maxIdleTime;

    @Autowired
    public BasicAuthentication(UserService userService, SessionService sessionService,
                               PasswordEncoder passwordEncoder, SessionProperties sessionProperties) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.maxIdleTime = Duration.ofDays(sessionProperties.getMaxIdleDays());
    }

    @Override
    public Mono<Authentication> apply(String authorization) {
        String[] tokens = extractAndDecodeToken(authorization);
        Assert.isTrue(tokens.length == 2, "Tokens should contain login and password");

        return userService.findByLogin(tokens[0]).filter(
                user -> passwordEncoder.matches(tokens[1], user.getPassword())
        ).switchIfEmpty(
                Mono.error(new BadCredentialsException("Login or Password not correct"))
        ).flatMap(user -> {
                    final Session session = Session.builder()
                            .id(Session.nextSessionId(user.getId()))
                            .userId(user.getId())
                            .username(user.getUsername())
                            .roles(user.getRoles())
                            .login(tokens[0])
                            .maxIdleTime(maxIdleTime)
                            .build();
                    return sessionService.saveUserSession(session).flatMap(
                            savedSession -> sessionService.expire(savedSession).map(
                                    expiredSession -> new AuthenticationToken(
                                            user.getId(),
                                            user.getPassword(),
                                            expiredSession,
                                            User.getAuthorities(user.getRoles())
                                    )
                            )
                    );
                }
        );
    }

    private String[] extractAndDecodeToken(String header) {
        try {
            byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
            String token = new String(Base64.getDecoder().decode(base64Token));
            int delimiterIndex = token.indexOf(":");
            if (delimiterIndex == -1) {
                throw new BadCredentialsException("Invalid Basic Authentication Token");
            }
            return new String[]{token.substring(0, delimiterIndex), token.substring(delimiterIndex + 1)};
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode Basic Authentication Token");
        }
    }
}
