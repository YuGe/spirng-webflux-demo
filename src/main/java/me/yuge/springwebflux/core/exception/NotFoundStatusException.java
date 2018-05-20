package me.yuge.springwebflux.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundStatusException extends ResponseStatusException {
    public NotFoundStatusException() {
        super(HttpStatus.NOT_FOUND);
    }
}
