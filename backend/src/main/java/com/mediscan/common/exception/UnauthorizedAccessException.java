package com.mediscan.common.exception;

/**
 * Exception thrown when a user attempts to access or modify a resource
 * they do not own or lack permissions for.
 * Results in HTTP 403 response.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
