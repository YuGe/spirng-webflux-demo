package me.yuge.springwebflux.initializer;

import lombok.extern.slf4j.Slf4j;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.repository.UserRepository;
import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class DataInitializer {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(TweetRepository tweetRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

//    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        initPosts();
        initUsers();
    }

    private void initUsers() {
        log.info("start userRepository initialization  ...");
        this.userRepository.deleteAll().thenMany(
                Flux.just("user", "admin").flatMap(username -> {
                    String[] roles = "user".equals(username)
                            ? new String[]{"USER"}
                            : new String[]{"USER", "ADMIN"};
                    User user = User.builder()
                            .username(username)
                            .password(passwordEncoder.encode("FooBar123"))
                            .roles(roles)
                            .build();
                    return this.userRepository.save(user);
                })
        ).subscribe(null, null, () -> log.info("done userRepository initialization..."));
    }

    private void initPosts() {
        log.info("start tweetRepository initialization  ...");
        this.tweetRepository.deleteAll().thenMany(
                Flux.just("Tweet one", "Tweet two", "推特 3").flatMap(
                        text -> this.tweetRepository.save(Tweet.builder().text(text).build())
                )
        ).subscribe(null, null, () -> log.info("done initialization..."));
    }
}
