package com.revhire.userservice.mapper;

import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.entity.EmployerProfile;
import com.revhire.userservice.entity.JobSeekerProfile;
import com.revhire.userservice.entity.User;

public class ProfileMapper {

    private ProfileMapper() {
        // Utility class
    }

    public static UserProfileResponse toResponse(User user, JobSeekerProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        
        if (profile != null) {
            response.setFirstName(profile.getFirstName());
            response.setLastName(profile.getLastName());
            response.setPhone(profile.getPhone());
            response.setDateOfBirth(profile.getDateOfBirth());
        }
        return response;
    }

    public static UserProfileResponse toResponse(User user, EmployerProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        
        if (profile != null) {
            response.setCompanyName(profile.getCompanyName());
            response.setContactName(profile.getContactName());
            response.setWebsite(profile.getWebsite());
        }
        return response;
    }
}
