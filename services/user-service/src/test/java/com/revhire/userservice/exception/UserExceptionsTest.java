package com.revhire.userservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserExceptionsTest {

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found message");
        assertEquals("Not found message", ex.getMessage());
    }

    @Test
    void testUserAlreadyExistsException() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Already exists message");
        assertEquals("Already exists message", ex.getMessage());
    }
}
