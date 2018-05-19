package me.yuge.springwebflux.initializer;

import lombok.extern.slf4j.Slf4j;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.repository.UserRepository;
import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;


@Slf4j
@Component
public class DataInitializer {
    private final Environment environment;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TweetRepository tweetRepository;
    private final ReactiveRedisConnection connection;

    @Autowired
    public DataInitializer(Environment environment,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TweetRepository tweetRepository,
                           ReactiveRedisConnectionFactory connectionFactory) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tweetRepository = tweetRepository;
        this.connection = connectionFactory.getReactiveConnection();
    }

//    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("dev")))) {
            initRedis();
            initUsers();
            initTweets();
        }
    }

    private void initRedis() {
        log.info("start redis initialization...");
        connection.serverCommands().flushAll().block(Duration.ofSeconds(5));
        log.info("done redis initialization...");
    }

    private void initUsers() {
        log.info("start userRepository initialization...");
        this.userRepository.deleteAll().thenMany(
                Flux.just("foo", "admin").flatMap(username -> {
                    String[] roles = "admin".equals(username)
                            ? new String[]{"ADMIN"}
                            : new String[]{};
                    String email = username + "@bar.com";
                    User user = User.builder()
                            .username(username)
                            .email(email)
                            .password(passwordEncoder.encode("FooBar123"))
                            .roles(roles)
                            .login(new String[]{username, email})
                            .build();
                    return this.userRepository.save(user);
                })
        ).blockLast(Duration.ofSeconds(5));
        log.info("done userRepository initialization...");
    }

    private void initTweets() {
        log.info("start tweetRepository initialization...");
        this.tweetRepository.deleteAll().thenMany(
                Flux.just("Tweet one", "Tweet 2", "推特三").flatMap(
                        text -> this.tweetRepository.save(new Tweet(text))
                )
        ).blockLast(Duration.ofSeconds(5));
        log.info("done tweetRepository initialization...");
    }
}
