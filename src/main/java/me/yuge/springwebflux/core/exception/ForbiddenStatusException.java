package me.yuge.springwebflux.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class ForbiddenStatusException extends ResponseStatusException {
    public ForbiddenStatusException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
