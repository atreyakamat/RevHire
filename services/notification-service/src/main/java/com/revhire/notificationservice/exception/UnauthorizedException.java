package com.revhire.notificationservice.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UnauthorizedException userNotAllowed(Long userId, Long notificationId) {
        return new UnauthorizedException(
                "User " + userId + " is not authorized to access notification " + notificationId
        );
    }
}