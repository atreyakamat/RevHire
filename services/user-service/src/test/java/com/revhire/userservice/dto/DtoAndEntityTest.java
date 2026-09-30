package com.revhire.userservice.dto;

import com.revhire.userservice.dto.request.ProfileUpdateRequest;
import com.revhire.userservice.dto.request.RoleUpdateRequest;
import com.revhire.userservice.dto.response.ErrorResponse;
import com.revhire.userservice.entity.EmployerProfile;
import com.revhire.userservice.entity.JobSeekerProfile;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndEntityTest {

    @Test
    void testRoleUpdateRequest() {
        RoleUpdateRequest req = new RoleUpdateRequest();
        req.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, req.getRole());

        RoleUpdateRequest req2 = new RoleUpdateRequest(Role.EMPLOYER);
        assertEquals(Role.EMPLOYER, req2.getRole());
    }

    @Test
    void testProfileUpdateRequest() {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setPhone("1234567890");
        req.setFirstName("First");
        req.setLastName("Last");
        LocalDate dob = LocalDate.of(1990, 1, 1);
        req.setDateOfBirth(dob);
        req.setCompanyName("Company");
        req.setContactName("Contact");
        req.setWebsite("https://example.com");

        assertEquals("1234567890", req.getPhone());
        assertEquals("First", req.getFirstName());
        assertEquals("Last", req.getLastName());
        assertEquals(dob, req.getDateOfBirth());
        assertEquals("Company", req.getCompanyName());
        assertEquals("Contact", req.getContactName());
        assertEquals("https://example.com", req.getWebsite());
    }

    @Test
    void testErrorResponse() {
        ErrorResponse resp = new ErrorResponse();
        LocalDateTime now = LocalDateTime.now();
        resp.setTimestamp(now);
        resp.setStatus(404);
        resp.setError("Not Found");
        resp.setMessage("Resource missing");
        resp.setPath("/api/test");

        assertEquals(now, resp.getTimestamp());
        assertEquals(404, resp.getStatus());
        assertEquals("Not Found", resp.getError());
        assertEquals("Resource missing", resp.getMessage());
        assertEquals("/api/test", resp.getPath());

        ErrorResponse resp2 = new ErrorResponse(now, 500, "Error", "Fail", "/fail");
        assertEquals(500, resp2.getStatus());
    }

    @Test
    void testUserEntity() {
        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");
        user.setPassword("hashedpass");
        user.setRole(Role.JOB_SEEKER);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(10L, user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("hashedpass", user.getPassword());
        assertEquals(Role.JOB_SEEKER, user.getRole());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());

        User user2 = new User("user@example.com", "hashedpass", Role.JOB_SEEKER);
        user2.setId(10L);
        assertEquals(user, user2);
        assertEquals(user.hashCode(), user2.hashCode());
        assertNotEquals(user, new Object());

        User diffUser = new User("diff@example.com", "hashedpass", Role.JOB_SEEKER);
        diffUser.setId(20L);
        assertNotEquals(user, diffUser);
    }

    @Test
    void testJobSeekerProfileEntity() {
        User user = new User("seeker@test.com", "pass", Role.JOB_SEEKER);
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setId(1L);
        profile.setUser(user);
        profile.setFirstName("Alice");
        profile.setLastName("Smith");
        profile.setPhone("5551234");
        LocalDate dob = LocalDate.of(1992, 3, 4);
        profile.setDateOfBirth(dob);

        assertEquals(1L, profile.getId());
        assertEquals(user, profile.getUser());
        assertEquals("Alice", profile.getFirstName());
        assertEquals("Smith", profile.getLastName());
        assertEquals("5551234", profile.getPhone());
        assertEquals(dob, profile.getDateOfBirth());
    }

    @Test
    void testEmployerProfileEntity() {
        User user = new User("emp@test.com", "pass", Role.EMPLOYER);
        EmployerProfile profile = new EmployerProfile();
        profile.setId(2L);
        profile.setUser(user);
        profile.setCompanyName("Acme");
        profile.setContactName("Bob");
        profile.setWebsite("https://acme.org");

        assertEquals(2L, profile.getId());
        assertEquals(user, profile.getUser());
        assertEquals("Acme", profile.getCompanyName());
        assertEquals("Bob", profile.getContactName());
        assertEquals("https://acme.org", profile.getWebsite());
    }
}
