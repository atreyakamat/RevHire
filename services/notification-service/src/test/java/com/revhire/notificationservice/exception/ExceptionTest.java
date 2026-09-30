package com.revhire.notificationservice.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testNotificationNotFoundException() {
        NotificationNotFoundException ex = NotificationNotFoundException.withId(123L);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("123"));

        NotificationNotFoundException exMsg = new NotificationNotFoundException("Custom not found");
        assertEquals("Custom not found", exMsg.getMessage());
    }

    @Test
    void testUnauthorizedException() {
        UnauthorizedException ex = UnauthorizedException.userNotAllowed(10L, 20L);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("10"));
        assertTrue(ex.getMessage().contains("20"));

        UnauthorizedException exMsg = new UnauthorizedException("Access denied");
        assertEquals("Access denied", exMsg.getMessage());
    }

    @Test
    void testErrorResponse() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Notification not found")
                .path("/api/notifications/1")
                .timestamp(now)
                .build();

        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Notification not found", response.getMessage());
        assertEquals("/api/notifications/1", response.getPath());
        assertEquals(now, response.getTimestamp());
    }
}
