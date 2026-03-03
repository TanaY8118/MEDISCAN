package com.mediscan.common.exception;

/**
 * Exception thrown when a request conflicts with the current state of the
 * server.
 * E.g., username already taken, group name already exists.
 * Results in HTTP 409 response.
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
