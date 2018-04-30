package me.yuge.springwebflux.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.security.SecureRandom;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Id
    private String id;
    private String userId;
    private String username;

    @Builder.Default()
    private String[] roles = new String[]{};

    public static String nextSessionId(String prefix) {
        // make sure the length of rnd hex string is 16
        final long rnd = SECURE_RANDOM.nextLong() | (1L << 62);
        return prefix + Long.toHexString(rnd);
    }


}
