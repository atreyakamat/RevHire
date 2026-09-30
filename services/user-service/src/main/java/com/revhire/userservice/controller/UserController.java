package com.revhire.userservice.controller;

import com.revhire.userservice.dto.request.ProfileUpdateRequest;
import com.revhire.userservice.dto.request.RoleUpdateRequest;
import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.security.CustomUserDetails;
import com.revhire.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new InsufficientAuthenticationException("User is not authenticated or principal is invalid");
        }
        Long userId = userDetails.getId();
        if (userId == null) {
            throw new InsufficientAuthenticationException("User ID not found in security context");
        }
        return userId;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        Long userId = getAuthenticatedUserId();
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(@RequestBody ProfileUpdateRequest request) {
        Long userId = getAuthenticatedUserId();
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYER')")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable("id") Long id) {
        UserProfileResponse response = userService.getUserProfile(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateUserRole(
            @PathVariable("id") Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        UserProfileResponse response = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(response);
    }
}
