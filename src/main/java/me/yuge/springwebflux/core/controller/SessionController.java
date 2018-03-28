package me.yuge.springwebflux.core.controller;

import me.yuge.springwebflux.core.model.Session;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/session")
public class SessionController {

    @GetMapping
    public Mono<Session> get() {
        return ReactiveSecurityContextHolder.getContext().map(
                securityContext -> securityContext.getAuthentication().getDetails()
        ).cast(Session.class);
    }

    @PostMapping
    public Mono<Session> create() {
        return ReactiveSecurityContextHolder.getContext().map(
                securityContext -> securityContext.getAuthentication().getDetails()
        ).cast(Session.class);
    }

}
