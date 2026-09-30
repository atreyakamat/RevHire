package com.revhire.resumeservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResumeGlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resume not found");
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resume not found", response.getBody().get("message"));
    }

    @Test
    void testHandleUnauthorizedAccess() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Forbidden");
        ResponseEntity<Map<String, Object>> response = handler.handleUnauthorizedAccess(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().get("message"));
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad arg");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad arg", response.getBody().get("message"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Crash");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected internal error occurred", response.getBody().get("message"));
    }
}
