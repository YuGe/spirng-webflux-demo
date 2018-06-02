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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

@Component
public class BasicAuthenticationConverter implements Function<String, Mono<Authentication>> {
    static final String BASIC = "Basic ";

    private final UserService userService;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final Duration maxIdleTime;

    @Autowired
    public BasicAuthenticationConverter(UserService userService, SessionService sessionService,
                                        PasswordEncoder passwordEncoder, SessionProperties sessionProperties) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.maxIdleTime = Duration.ofDays(sessionProperties.getMaxIdleDays());
    }

    @Override
    public Mono<Authentication> apply(String authorization) {
        Optional<String[]> extractCredentials = decodeAndExtractCredentials(authorization);
        if (!extractCredentials.isPresent()) {
            return Mono.error(new BadCredentialsException("Basic Credentials Format Error"));
        }
        final String[] loginPassword = extractCredentials.get();

        return userService.findByLogin(loginPassword[0]).publishOn(Schedulers.parallel()).filter(
                user -> passwordEncoder.matches(loginPassword[1], user.getPassword())
        ).switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid Basic Credentials")))
        ).flatMap(user -> {
                    final Session session = Session.builder()
                            .id(Session.nextSessionId(user.getId()))
                            .userId(user.getId())
                            .username(user.getUsername())
                            .roles(user.getRoles())
                            .login(loginPassword[0])
                            .maxIdleTime(maxIdleTime)
                            .build();
                    return sessionService.saveUserSession(session).flatMap(
                            savedSession -> sessionService.expire(savedSession).map(
                                    expiredSession -> new SessionDetailsAuthenticationToken(
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

    /**
     * Decode and extract login, password from authorization header.
     *
     * @param header The basic authorization header.
     * @return String[] with login and password.
     */
    private Optional<String[]> decodeAndExtractCredentials(String header) {
        String credentials = new String(base64Decode(header.substring(BASIC.length())));
        String[] loginPassword = credentials.split(":");
        if (loginPassword.length != 2) {
            return Optional.empty();
        }
        return Optional.of(loginPassword);
    }

    private byte[] base64Decode(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
