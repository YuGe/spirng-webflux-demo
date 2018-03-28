package me.yuge.springwebflux.core.security;

import me.yuge.springwebflux.core.repository.UserRepository;
import me.yuge.springwebflux.core.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

@Component
public class BasicAuthentication implements Function<String, Mono<Authentication>> {

    static final String BASIC = "Basic ";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SessionService sessionService;

    @Autowired
    public BasicAuthentication(PasswordEncoder passwordEncoder, UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Override
    public Mono<Authentication> apply(String authorization) {
        String[] tokens;
        tokens = extractAndDecodeHeader(authorization);
        assert tokens.length == 2;

        return userRepository.findByUsername(tokens[0]).filter(
                user -> passwordEncoder.matches(tokens[1], user.getPassword())
        ).switchIfEmpty(Mono.error(
                new BadCredentialsException("Login or password not correct")
        )).flatMap(user -> sessionService.create(user).map(
                session -> new AuthenticationToken(user.getId(), user.getPassword(), session, user.getAuthorities())
        ));
    }


    private String[] extractAndDecodeHeader(String header) {

        byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        String token = new String(decoded);

        int delimiterIndex = token.indexOf(":");
        if (delimiterIndex == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return new String[]{token.substring(0, delimiterIndex), token.substring(delimiterIndex + 1)};
    }
}
