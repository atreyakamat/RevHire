package com.revhire.userservice.mapper;

import com.revhire.userservice.dto.response.AuthResponse;
import com.revhire.userservice.entity.User;

public class UserMapper {
    
    private UserMapper() {
        // Utility class
    }

    public static AuthResponse toAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setRole(user.getRole().name());
        return response;
    }
}
