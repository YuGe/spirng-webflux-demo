package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.exception.ForbiddenStatusException;
import me.yuge.springwebflux.core.exception.NotFoundStatusException;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.model.Verification;
import me.yuge.springwebflux.core.service.SessionService;
import me.yuge.springwebflux.core.service.UserService;
import me.yuge.springwebflux.core.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.Objects;

@RestController
@RequestMapping("sessions")
@PreAuthorize("isAuthenticated()")
public class SessionController {
    private final UserService userService;
    private final SessionService sessionService;
    private final VerificationService verificationService;

    @Autowired
    public SessionController(UserService userService, SessionService sessionService, VerificationService verificationService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.verificationService = verificationService;
    }

    @PostMapping
    public Mono<Session> post() {
        return sessionService.getAuthenticatedSession();
    }

    @GetMapping("{id}")
    public Mono<Session> get(@PathVariable(value = "id") String id) {
        return sessionService.getAuthenticatedSession()
                .flatMap(authSession -> sessionService.get(id)
                        .switchIfEmpty(Mono.error(new NotFoundStatusException()))
                        .filter(targetSession -> Objects.equals(targetSession.getUserId(), authSession.getUserId()))
                        .switchIfEmpty(Mono.error(new ForbiddenStatusException("Not allowed to get the session"))));
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable(value = "id") String id) {
        return sessionService.getAuthenticatedSession()
                .flatMap(authSession -> sessionService.get(id)
                        .switchIfEmpty(Mono.error(new NotFoundStatusException()))
                        .filter(targetSession -> Objects.equals(targetSession.getUserId(), authSession.getUserId()))
                        .switchIfEmpty(Mono.error(new ForbiddenStatusException("Not allowed to delete this session"))))
                .flatMap(session -> sessionService.delete(session.getId()))
                .then();
    }

    @PreAuthorize("permitAll()")
    @PostMapping("verification/email")
    public Mono<Void> emailVerification(@Valid @RequestBody Verification verification) {
        return userService.findByLogin(verification.getLogin())
                .switchIfEmpty(Mono.error(new ValidationException()))
                .flatMap(user -> verificationService.create(verification))
                .flatMap(verificationService::emailCode)
                .then();
    }

    @PreAuthorize("permitAll()")
    @PutMapping("verification/email")
    public Mono<Void> verifyVerification(@Valid @RequestBody Verification verification) {
        return verificationService.verify(verification)
                .filter(Boolean::booleanValue)
//                .switchIfEmpty()
                .then();
    }
}
