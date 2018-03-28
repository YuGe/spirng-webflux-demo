package me.yuge.springwebflux.controller;

import me.yuge.springwebflux.model.User;
import me.yuge.springwebflux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("user")
    public Mono<User> current(@AuthenticationPrincipal Mono<User> principal) {
        return principal;
    }

    @GetMapping("user/{username}")
    public Mono<User> get(@PathVariable() String username) {
        return this.userRepository.findByUsername(username);
    }
}
