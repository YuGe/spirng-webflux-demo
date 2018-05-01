package me.yuge.springwebflux.core.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;


@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Id
    private String id;
    private String userId;
    private String username;
    private String login;
    @Builder.Default
    private String[] roles = new String[]{};
    @Builder.Default
    private Instant createdTime = Instant.now();
    private Duration maxIdleTime;
    @Builder.Default
    private boolean verified = false;

    public static String nextSessionId(String userId) {
        // make sure the length of rnd hex string is 16
        final long rnd = SECURE_RANDOM.nextLong() | (1L << 62);
        return userId + Long.toHexString(rnd);
    }

}
