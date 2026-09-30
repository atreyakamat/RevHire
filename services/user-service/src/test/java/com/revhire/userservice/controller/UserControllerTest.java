package com.revhire.userservice.controller;

import com.revhire.userservice.dto.request.ProfileUpdateRequest;
import com.revhire.userservice.dto.request.RoleUpdateRequest;
import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import com.revhire.userservice.security.CustomUserDetails;
import com.revhire.userservice.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUser_Success() {
        User user = new User("test@revhire.local", "secret", Role.JOB_SEEKER);
        user.setId(10L);
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserProfileResponse mockProfile = new UserProfileResponse();
        mockProfile.setId(10L);
        mockProfile.setEmail("test@revhire.local");
        mockProfile.setRole(Role.JOB_SEEKER);
        mockProfile.setFirstName("Test");
        mockProfile.setLastName("User");

        when(userService.getUserProfile(10L)).thenReturn(mockProfile);

        ResponseEntity<UserProfileResponse> response = userController.getCurrentUser();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("test@revhire.local", response.getBody().getEmail());
        verify(userService, times(1)).getUserProfile(10L);
    }

    @Test
    void testGetCurrentUser_MissingAuthentication_ThrowsException() {
        SecurityContextHolder.clearContext();

        InsufficientAuthenticationException ex = assertThrows(
                InsufficientAuthenticationException.class,
                () -> userController.getCurrentUser()
        );
        assertTrue(ex.getMessage().contains("User is not authenticated"));
        verifyNoInteractions(userService);
    }

    @Test
    void testGetCurrentUser_InvalidPrincipalType_ThrowsException() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        InsufficientAuthenticationException ex = assertThrows(
                InsufficientAuthenticationException.class,
                () -> userController.getCurrentUser()
        );
        assertTrue(ex.getMessage().contains("User is not authenticated or principal is invalid"));
        verifyNoInteractions(userService);
    }

    @Test
    void testGetCurrentUser_NullUserId_ThrowsException() {
        User user = new User("test@revhire.local", "secret", Role.JOB_SEEKER);
        user.setId(null);
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        InsufficientAuthenticationException ex = assertThrows(
                InsufficientAuthenticationException.class,
                () -> userController.getCurrentUser()
        );
        assertTrue(ex.getMessage().contains("User ID not found"));
        verifyNoInteractions(userService);
    }

    @Test
    void testGetUserById_Success() {
        UserProfileResponse mockProfile = new UserProfileResponse();
        mockProfile.setId(5L);
        mockProfile.setEmail("user5@revhire.local");
        when(userService.getUserProfile(5L)).thenReturn(mockProfile);

        ResponseEntity<UserProfileResponse> response = userController.getUserById(5L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().getId());
        verify(userService, times(1)).getUserProfile(5L);
    }

    @Test
    void testUpdateCurrentUserProfile_Success() {
        User user = new User("test@revhire.local", "secret", Role.JOB_SEEKER);
        user.setId(10L);
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ProfileUpdateRequest updateReq = new ProfileUpdateRequest();
        updateReq.setFirstName("Updated");

        UserProfileResponse mockProfile = new UserProfileResponse();
        mockProfile.setId(10L);
        mockProfile.setFirstName("Updated");
        mockProfile.setRole(Role.JOB_SEEKER);

        when(userService.updateUserProfile(eq(10L), any(ProfileUpdateRequest.class))).thenReturn(mockProfile);

        ResponseEntity<UserProfileResponse> response = userController.updateCurrentUserProfile(updateReq);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getFirstName());
        verify(userService, times(1)).updateUserProfile(eq(10L), any(ProfileUpdateRequest.class));
    }

    @Test
    void testUpdateUserRole_Success() {
        RoleUpdateRequest req = new RoleUpdateRequest(Role.ADMIN);
        UserProfileResponse mockProfile = new UserProfileResponse();
        mockProfile.setId(15L);
        mockProfile.setRole(Role.ADMIN);

        when(userService.updateUserRole(15L, Role.ADMIN)).thenReturn(mockProfile);

        ResponseEntity<UserProfileResponse> response = userController.updateUserRole(15L, req);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Role.ADMIN, response.getBody().getRole());
        verify(userService, times(1)).updateUserRole(15L, Role.ADMIN);
    }
}
