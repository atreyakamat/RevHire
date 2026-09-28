package com.revhire.userservice.exception;

import com.revhire.userservice.dto.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/test");
        webRequest = new ServletWebRequest(servletRequest);
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void testHandleUserAlreadyExistsException() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Email exists");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserAlreadyExistsException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Email exists", response.getBody().getMessage());
    }

    @Test
    void testHandleAuthenticationException() {
        BadCredentialsException ex = new BadCredentialsException("Invalid password");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid password", response.getBody().getMessage());
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad input");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad input", response.getBody().getMessage());
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected internal error occurred", response.getBody().getMessage());
    }
}
