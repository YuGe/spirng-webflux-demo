package me.yuge.springwebflux.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.SecureRandom;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Verification {
    @NotBlank
    private String login;
    @Size(min = 6, max = 6)
    private String code;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String nextCode() {
        // make sure the length of random number is 6
        final int random = SECURE_RANDOM.nextInt(1000000) | (1 << 20);
        final String randomString = Integer.toString(random);
        return randomString.substring(randomString.length() - 6, randomString.length());
    }
}
