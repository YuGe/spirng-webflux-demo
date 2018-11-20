package me.yuge.springwebflux.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ConflictStatusException extends ResponseStatusException {
    public ConflictStatusException() {
        super(HttpStatus.CONFLICT);
    }
}
