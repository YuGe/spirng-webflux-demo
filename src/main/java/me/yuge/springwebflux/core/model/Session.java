package me.yuge.springwebflux.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private String id;
    private String userId;
    private String username;
    private String login;
    @Builder.Default
    private String[] roles = new String[]{};
    private Duration maxIdleTime;
    @Builder.Default
    private Instant createdTime = Instant.now();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String nextSessionId(String userId) {
        // make sure the length of random hex string is 16
        final long random = SECURE_RANDOM.nextLong() | (1L << 62);
        return userId + Long.toHexString(random);
    }
}
