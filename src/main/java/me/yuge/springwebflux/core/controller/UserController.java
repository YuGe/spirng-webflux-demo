package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.exception.NotFoundException;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequestMapping("users")
@RestController
public class UserController {
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Flux<User> all() {
        return userRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<User> get(@PathVariable() String id) {
        return userRepository.findById(id);
    }

    @GetMapping(params = "username")
    public Mono<User> getByUsername(@RequestParam("username") String username) {
        return userRepository.findByUsername(username).switchIfEmpty(Mono.error(new NotFoundException()));
    }

    @GetMapping(params = "login")
    public Mono<User> getByLogin(@RequestParam("login") String login) {
        return userRepository.findByLogin(login);
    }

}
