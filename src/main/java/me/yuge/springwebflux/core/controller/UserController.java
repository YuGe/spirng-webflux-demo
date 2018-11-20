package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.exception.ConflictStatusException;
import me.yuge.springwebflux.core.exception.ForbiddenStatusException;
import me.yuge.springwebflux.core.exception.NotFoundStatusException;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.model.User;
import me.yuge.springwebflux.core.model.Verification;
import me.yuge.springwebflux.core.service.SessionService;
import me.yuge.springwebflux.core.service.UserRepository;
import me.yuge.springwebflux.core.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("users")
@PreAuthorize("isAuthenticated()")
public class UserController {
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final VerificationService verificationService;

    @Autowired
    public UserController(UserRepository userRepository, SessionService sessionService, VerificationService verificationService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.verificationService = verificationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<User> all() {
        return userRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<User> get(@PathVariable String id) {
        return userRepository.findById(id);
    }

    @GetMapping(params = "login")
    public Mono<User> getByLogin(@RequestParam("login") String login) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new NotFoundStatusException()));
    }

    @GetMapping("{userId}/sessions")
    public Flux<Session> getUserSessionAll(@PathVariable String userId) {
        return sessionService.getAuthenticatedSession()
                .filter(session -> Objects.equals(session.getUserId(), userId))
                .switchIfEmpty(Mono.error(new ForbiddenStatusException("Not allowed to get the user's sessions")))
                .flatMapMany(session -> sessionService.getUserSessionAll(userId));
    }

    @DeleteMapping("{userId}/sessions")
    public Mono<Void> deleteUserSessionAll(@PathVariable String userId) {
        return sessionService.getAuthenticatedSession()
                .filter(session -> Objects.equals(session.getUserId(), userId))
                .switchIfEmpty(Mono.error(new ForbiddenStatusException("Not allowed to delete the user's sessions")))
                .flatMap(session -> sessionService.deleteUserSessionAll(userId))
                .then();
    }

    @PreAuthorize("permitAll()")
    @PostMapping("verification/email")
    public Mono<Void> emailVerification(@Valid @RequestBody Verification verification) {
        return userRepository.findByLogin(verification.getLogin())
                .flatMap(user -> Mono.error(new ConflictStatusException()))
                .switchIfEmpty(verificationService
                        .create(verification)
                        .flatMap(verificationService::emailCode))
                .then();
    }

    @PreAuthorize("permitAll()")
    @PutMapping("verification/email")
    public Mono<Void> verifyVerification(@Valid @RequestBody Verification verification) {
        return verificationService.verify(verification)
                .filter(Boolean::booleanValue)
//                .switchIfEmpty(new WebExchangeBindException("login", new))
                .then();
    }
}
