package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.LoginRequest;
import com.revhire.userservice.dto.request.UserRegistrationRequest;
import com.revhire.userservice.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerUser(UserRegistrationRequest request);
    AuthResponse authenticateUser(LoginRequest request);
}
