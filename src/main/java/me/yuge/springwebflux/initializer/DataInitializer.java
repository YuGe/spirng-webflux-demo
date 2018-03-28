package me.yuge.springwebflux.initializer;

import lombok.extern.slf4j.Slf4j;
import me.yuge.springwebflux.demo.model.Post;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.demo.repository.PostRepository;
import me.yuge.springwebflux.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class DataInitializer {
    private final PostRepository posts;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(PostRepository posts, UserRepository users, PasswordEncoder passwordEncoder) {
        this.posts = posts;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        initPosts();
        initUsers();
    }

    private void initUsers() {
        log.info("start users initialization  ...");
        this.users
                .deleteAll()
                .thenMany(Flux.just("user", "admin")
                        .flatMap(username ->
                                {
                                    String[] roles = "user".equals(username)
                                            ? new String[]{"USER"}
                                            : new String[]{"USER", "ADMIN"};

                                    User user = User.builder()
                                            .username(username)
                                            .password(passwordEncoder.encode("FooBar123"))
                                            .roles(roles)
                                            .build();
                                    return this.users.save(user);
                                }
                        )
                )
                .subscribe(
                        null,
                        null,
                        () -> log.info("done users initialization...")
                );
    }

    private void initPosts() {
        log.info("start data initialization  ...");
        this.posts.deleteAll()
                .thenMany(Flux.just("Post one", "Post two")
                        .flatMap(title -> this.posts.save(Post.builder().title(title).content("content of " + title).build()))
                )
                .log()
                .subscribe(
                        null,
                        null,
                        () -> log.info("done initialization...")
                );
    }
}
