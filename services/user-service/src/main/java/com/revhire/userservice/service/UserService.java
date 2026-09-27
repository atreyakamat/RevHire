package com.revhire.userservice.service;

import com.revhire.userservice.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getUserProfile(Long userId);
}
