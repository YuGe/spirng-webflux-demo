package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("users")
    public Flux<User> all() {
        return userRepository.findAll();
    }

    @GetMapping("users/{id}")
    public Mono<User> get(@PathVariable() String id) {
        return userRepository.findById(id);
    }

}
