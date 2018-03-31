package me.yuge.springwebflux.initializer;

import lombok.extern.slf4j.Slf4j;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.repository.UserRepository;
import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class DataInitializer {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory;

    @Autowired
    public DataInitializer(TweetRepository tweetRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.reactiveMongoDatabaseFactory = reactiveMongoDatabaseFactory;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        reactiveMongoDatabaseFactory.getMongoDatabase().drop();
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
                            .email(username + "@bar.com")
                            .password(passwordEncoder.encode("FooBar123"))
                            .roles(roles)
                            .login(new String[]{username, username + "@bar.com"})
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
