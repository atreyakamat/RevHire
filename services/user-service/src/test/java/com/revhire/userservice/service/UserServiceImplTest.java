package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.ProfileUpdateRequest;
import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.entity.EmployerProfile;
import com.revhire.userservice.entity.JobSeekerProfile;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import com.revhire.userservice.exception.ResourceNotFoundException;
import com.revhire.userservice.repository.EmployerProfileRepository;
import com.revhire.userservice.repository.JobSeekerProfileRepository;
import com.revhire.userservice.repository.UserRepository;
import com.revhire.userservice.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetUserProfile_JobSeeker_Success() {
        User user = new User("seeker@revhire.local", "pass", Role.JOB_SEEKER);
        user.setId(1L);
        JobSeekerProfile profile = new JobSeekerProfile(user, "Jane", "Doe", "555-1234", LocalDate.of(1995, 5, 20));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jobSeekerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        UserProfileResponse response = userService.getUserProfile(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("seeker@revhire.local", response.getEmail());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(Role.JOB_SEEKER, response.getRole());
    }

    @Test
    void testGetUserProfile_Employer_Success() {
        User user = new User("emp@revhire.local", "pass", Role.EMPLOYER);
        user.setId(2L);
        EmployerProfile profile = new EmployerProfile(user, "Acme Corp", "John Smith", "https://acme.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(employerProfileRepository.findById(2L)).thenReturn(Optional.of(profile));

        UserProfileResponse response = userService.getUserProfile(2L);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Acme Corp", response.getCompanyName());
        assertEquals(Role.EMPLOYER, response.getRole());
    }

    @Test
    void testGetUserProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile(99L));
    }

    @Test
    void testGetUserProfile_NullUserId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserProfile(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    void testGetUserProfile_EmployerViewingAnotherEmployer_ThrowsAccessDenied() {
        User targetUser = new User("otheremp@revhire.local", "pass", Role.EMPLOYER);
        targetUser.setId(3L);

        User currentAuthUser = new User("currentemp@revhire.local", "pass", Role.EMPLOYER);
        currentAuthUser.setId(2L);
        CustomUserDetails userDetails = CustomUserDetails.create(currentAuthUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(3L)).thenReturn(Optional.of(targetUser));

        assertThrows(AccessDeniedException.class, () -> userService.getUserProfile(3L));
    }

    @Test
    void testUpdateUserProfile_JobSeeker_DoesNotChangeRoleOrId() {
        User user = new User("seeker@revhire.local", "pass", Role.JOB_SEEKER);
        user.setId(1L);
        JobSeekerProfile profile = new JobSeekerProfile(user, "OldFirst", "OldLast", "111", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jobSeekerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        ProfileUpdateRequest updateReq = new ProfileUpdateRequest();
        updateReq.setFirstName("NewFirst");
        updateReq.setLastName("NewLast");
        updateReq.setPhone("999");

        UserProfileResponse response = userService.updateUserProfile(1L, updateReq);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(Role.JOB_SEEKER, response.getRole()); // role preserved
        assertEquals("NewFirst", response.getFirstName());
        assertEquals("NewLast", response.getLastName());
        assertEquals("999", response.getPhone());
        verify(jobSeekerProfileRepository, times(1)).save(profile);
    }

    @Test
    void testUpdateUserRole_Success() {
        User user = new User("user@revhire.local", "pass", Role.JOB_SEEKER);
        user.setId(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileResponse response = userService.updateUserRole(5L, Role.EMPLOYER);

        assertNotNull(response);
        assertEquals(Role.EMPLOYER, response.getRole());
        assertEquals(Role.EMPLOYER, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUserRole_NullRole_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserRole(5L, null));
    }

    @Test
    void testUpdateUserProfile_Employer_Success() {
        User user = new User("emp@revhire.local", "pass", Role.EMPLOYER);
        user.setId(2L);
        EmployerProfile profile = new EmployerProfile(user, "Old Company", "Old Contact", "https://old.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(employerProfileRepository.findById(2L)).thenReturn(Optional.of(profile));

        ProfileUpdateRequest updateReq = new ProfileUpdateRequest();
        updateReq.setCompanyName("New Company");
        updateReq.setContactName("New Contact");
        updateReq.setWebsite("https://new.com");

        UserProfileResponse response = userService.updateUserProfile(2L, updateReq);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(Role.EMPLOYER, response.getRole());
        assertEquals("New Company", response.getCompanyName());
        assertEquals("New Contact", response.getContactName());
        assertEquals("https://new.com", response.getWebsite());
        verify(employerProfileRepository, times(1)).save(profile);
    }

    @Test
    void testUpdateUserProfile_NullUserId_ThrowsException() {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserProfile(null, req));
    }

    @Test
    void testUpdateUserProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserProfile(99L, req));
    }

    @Test
    void testUpdateUserRole_NullUserId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserRole(null, Role.ADMIN));
    }

    @Test
    void testUpdateUserRole_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole(99L, Role.ADMIN));
    }

    @Test
    void testGetUserProfile_AdminUser() {
        User user = new User("admin@revhire.local", "pass", Role.ADMIN);
        user.setId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile(99L);
        assertNotNull(response);
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void testUpdateUserRole_ToJobSeeker() {
        User user = new User("user@revhire.local", "pass", Role.EMPLOYER);
        user.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileResponse response = userService.updateUserRole(5L, Role.JOB_SEEKER);
        assertNotNull(response);
        assertEquals(Role.JOB_SEEKER, response.getRole());
    }
}
