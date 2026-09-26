package com.revhire.notificationservice.exception;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static NotificationNotFoundException withId(Long id) {
        return new NotificationNotFoundException("Notification with ID " + id + " not found");
    }
}