package me.yuge.springwebflux.core.exception;


public class ForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 8405851137638775807L;

    public ForbiddenException(String message) {
        super(message);
    }
}
