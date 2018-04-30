package me.yuge.springwebflux.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.yuge.springwebflux.core.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;


@Data
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
public class Session implements WebSession {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Id
    private String id;
    private String userId;
    private String username;
    @Builder.Default()
    private String[] roles = new String[]{};
    @Builder.Default()
    private Instant createdTime = Instant.now();
    @Builder.Default()
    private Instant modifiedTime = Instant.now();

    private SessionService sessionService;

    @Autowired
    public Session(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public static String nextSessionId(String prefix) {
        // make sure the length of rnd hex string is 16
        final long rnd = SECURE_RANDOM.nextLong() | (1L << 62);
        return prefix + Long.toHexString(rnd);
    }


    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public Mono<Void> changeSessionId() {
        return null;
    }

    @Override
    public Mono<Void> invalidate() {
        return null;
    }

    @Override
    public Mono<Void> save() {
        return sessionService.save(this)
                .flatMap(session -> sessionService.expire(this)
                        .flatMap(s -> Mono.empty()));
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    @Transient
    public Instant getCreationTime() {
        return createdTime;
    }

    @Override
    @Transient
    public Instant getLastAccessTime() {
        return modifiedTime;
    }

    @Override
    @Transient
    public void setMaxIdleTime(Duration maxIdleTime) {

    }

    @Override
    @Transient
    public Duration getMaxIdleTime() {
        return null;
    }
}
