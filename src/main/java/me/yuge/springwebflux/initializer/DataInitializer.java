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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;


@Slf4j
@Component
public class DataInitializer {
    private final Environment environment;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TweetRepository tweetRepository;

    @Autowired
    public DataInitializer(Environment environment, UserRepository userRepository, PasswordEncoder passwordEncoder, TweetRepository tweetRepository) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tweetRepository = tweetRepository;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("dev")))) {
            initPosts();
            initUsers();
        }
    }

    private void initUsers() {
        log.info("start userRepository initialization  ...");
        this.userRepository.deleteAll().thenMany(
                Flux.just("foo", "admin").flatMap(username -> {
                    String[] roles = "admin".equals(username)
                            ? new String[]{User.Role.USER, User.Role.ADMIN}
                            : new String[]{User.Role.USER};
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
        ).subscribe(null, null, () -> log.info("done userRepository initialization..."));
    }

    private void initPosts() {
        log.info("start tweetRepository initialization  ...");
        this.tweetRepository.deleteAll().thenMany(
                Flux.just("Tweet one", "Tweet two", "推特 3").flatMap(
                        text -> this.tweetRepository.save(new Tweet(text))
                )
        ).subscribe(null, null, () -> log.info("done tweetRepository initialization..."));
    }
}
