package com.revhire.userservice.mapper;

import com.revhire.userservice.dto.response.AuthResponse;
import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.entity.EmployerProfile;
import com.revhire.userservice.entity.JobSeekerProfile;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    void testUserMapper() {
        User user = new User("user@revhire.com", "pass", Role.JOB_SEEKER);
        user.setId(5L);

        AuthResponse authResponse = UserMapper.toAuthResponse(user, "test.token");
        assertNotNull(authResponse);
        assertEquals(5L, authResponse.getUserId());
        assertEquals("JOB_SEEKER", authResponse.getRole());
        assertEquals("test.token", authResponse.getToken());
    }

    @Test
    void testProfileMapper_JobSeeker() {
        User user = new User("seeker@revhire.com", "pass", Role.JOB_SEEKER);
        user.setId(10L);

        JobSeekerProfile profile = new JobSeekerProfile(
                user,
                "John",
                "Doe",
                "1234567890",
                LocalDate.of(1990, 5, 15)
        );

        UserProfileResponse response = ProfileMapper.toResponse(user, profile);
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("seeker@revhire.com", response.getEmail());
        assertEquals(Role.JOB_SEEKER, response.getRole());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("1234567890", response.getPhone());
        assertEquals(LocalDate.of(1990, 5, 15), response.getDateOfBirth());

        UserProfileResponse nullProfileResponse = ProfileMapper.toResponse(user, (JobSeekerProfile) null);
        assertNotNull(nullProfileResponse);
        assertNull(nullProfileResponse.getFirstName());
    }

    @Test
    void testProfileMapper_Employer() {
        User user = new User("employer@revhire.com", "pass", Role.EMPLOYER);
        user.setId(20L);

        EmployerProfile profile = new EmployerProfile(
                user,
                "Tech Corp",
                "Jane",
                "https://techcorp.com"
        );

        UserProfileResponse response = ProfileMapper.toResponse(user, profile);
        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("Tech Corp", response.getCompanyName());
        assertEquals("Jane", response.getContactName());
        assertEquals("https://techcorp.com", response.getWebsite());

        UserProfileResponse nullProfileResponse = ProfileMapper.toResponse(user, (EmployerProfile) null);
        assertNotNull(nullProfileResponse);
        assertNull(nullProfileResponse.getCompanyName());
    }
}
