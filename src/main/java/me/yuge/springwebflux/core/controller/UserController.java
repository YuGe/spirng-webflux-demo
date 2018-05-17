package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.exception.ForbiddenStatusException;
import me.yuge.springwebflux.core.exception.NotFoundStatusException;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.repository.UserRepository;
import me.yuge.springwebflux.core.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;


@RestController
@RequestMapping("users")
public class UserController {
    private final UserRepository userRepository;
    private final SessionService sessionService;

    @Autowired
    public UserController(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    @GetMapping
    public Flux<User> all() {
        return userRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<User> get(@PathVariable String id) {
        return userRepository.findById(id);
    }

    @GetMapping(params = "username")
    public Mono<User> getByUsername(@RequestParam("username") String username) {
        return userRepository.findByUsername(username).switchIfEmpty(Mono.error(new NotFoundStatusException()));
    }

    @GetMapping(params = "login")
    public Mono<User> getByLogin(@RequestParam("login") String login) {
        return userRepository.findByLogin(login).switchIfEmpty(Mono.error(new NotFoundStatusException()));
    }

    @GetMapping("{userId}/sessions")
    public Flux<Session> getUserSessionAll(@PathVariable String userId) {
        return sessionService.getAuthenticatedSession().flatMapMany(
                savedSession -> Objects.equals(savedSession.getUserId(), userId)
                        ? sessionService.getUserSessionAll(userId)
                        : Mono.error(new ForbiddenStatusException("Not allowed to get the user's sessions"))
        );
    }

    @DeleteMapping("{userId}/sessions")
    public Mono<Void> deleteUserSessionAll(@PathVariable String userId) {
        return sessionService.getAuthenticatedSession().flatMap(
                savedSession -> Objects.equals(savedSession.getUserId(), userId)
                        ? sessionService.deleteUserSessionAll(userId)
                        : Mono.error(new ForbiddenStatusException("Not allowed to delete the user's sessions"))
        );
    }
}
