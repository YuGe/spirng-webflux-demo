package me.yuge.springwebflux.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 8405851137638775807L;

    public ForbiddenException() {

    }

    public ForbiddenException(String message) {
        super(message);
    }
}
