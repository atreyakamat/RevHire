package com.revhire.userservice.controller;

import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.security.CustomUserDetails;
import com.revhire.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("User is not authenticated or principal is invalid");
        }
        Long userId = userDetails.getId();
        if (userId == null) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("User ID not found in security context");
        }

        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYER')")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable("id") Long id) {
        UserProfileResponse response = userService.getUserProfile(id);
        return ResponseEntity.ok(response);
    }
}
