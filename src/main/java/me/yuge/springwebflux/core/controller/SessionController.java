package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.exception.ForbiddenStatusException;
import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Objects;


@RestController
@RequestMapping("session")
public class SessionController {
    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public Mono<Session> post() {
        return ReactiveSecurityContextHolder.getContext().map(
                securityContext -> securityContext.getAuthentication().getDetails()
        ).cast(Session.class);
    }

    @GetMapping("{id}")
    public Mono<Session> get(@PathVariable(value = "id") String id) {
        return ReactiveSecurityContextHolder.getContext().map(
                securityContext -> securityContext.getAuthentication().getDetails()
        ).cast(Session.class).flatMap(
                authSession -> sessionService.get(id).filter(
                        targetSession -> Objects.equals(targetSession.getUserId(), authSession.getUserId())
                ).switchIfEmpty(
                        Mono.error(new ForbiddenStatusException("Not allowed to access this session"))
                )
        );
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable(value = "id") String id) {
        return ReactiveSecurityContextHolder.getContext().map(
                securityContext -> securityContext.getAuthentication().getDetails()
        ).cast(Session.class).flatMap(
                authSession -> sessionService.get(id).filter(
                        targetSession -> Objects.equals(targetSession.getUserId(), authSession.getUserId())
                ).switchIfEmpty(
                        Mono.error(new ForbiddenStatusException("Not allowed to delete this session"))
                ).flatMap(session -> sessionService.delete(session.getId()))
        );
    }
}
