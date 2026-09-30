package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.ProfileUpdateRequest;
import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.entity.Role;

public interface UserService {
    UserProfileResponse getUserProfile(Long userId);
    UserProfileResponse updateUserProfile(Long userId, ProfileUpdateRequest request);
    UserProfileResponse updateUserRole(Long userId, Role newRole);
}
