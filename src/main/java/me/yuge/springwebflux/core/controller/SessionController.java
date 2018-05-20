package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.exception.ForbiddenStatusException;
import me.yuge.springwebflux.core.exception.NotFoundStatusException;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@RequestMapping("sessions")
@PreAuthorize("isAuthenticated()")
public class SessionController {
    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public Mono<Session> post() {
        return sessionService.getAuthenticatedSession();
    }

    @GetMapping("{id}")
    public Mono<Session> get(@PathVariable(value = "id") String id) {
        return sessionService.getAuthenticatedSession().flatMap(
                authSession -> sessionService.get(id).switchIfEmpty(
                        Mono.error(new NotFoundStatusException())
                ).filter(
                        targetSession -> Objects.equals(targetSession.getUserId(), authSession.getUserId())
                ).switchIfEmpty(
                        Mono.error(new ForbiddenStatusException("Not allowed to get the session"))
                )
        );
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable(value = "id") String id) {
        return sessionService.getAuthenticatedSession().flatMap(
                authSession -> sessionService.get(id).switchIfEmpty(
                        Mono.error(new NotFoundStatusException())
                ).filter(
                        targetSession -> Objects.equals(targetSession.getUserId(), authSession.getUserId())
                ).switchIfEmpty(
                        Mono.error(new ForbiddenStatusException("Not allowed to delete the session"))
                ).flatMap(session -> sessionService.delete(session.getId()))
        );
    }
}
