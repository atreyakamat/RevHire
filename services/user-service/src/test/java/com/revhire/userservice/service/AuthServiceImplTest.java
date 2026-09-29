package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.LoginRequest;
import com.revhire.userservice.dto.request.UserRegistrationRequest;
import com.revhire.userservice.dto.response.AuthResponse;
import com.revhire.userservice.entity.EmployerProfile;
import com.revhire.userservice.entity.JobSeekerProfile;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import com.revhire.userservice.exception.UserAlreadyExistsException;
import com.revhire.userservice.repository.EmployerProfileRepository;
import com.revhire.userservice.repository.JobSeekerProfileRepository;
import com.revhire.userservice.repository.UserRepository;
import com.revhire.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                jobSeekerProfileRepository,
                employerProfileRepository,
                passwordEncoder,
                tokenProvider
        );
    }

    @Test
    void testRegisterUser_JobSeeker_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("seeker@revhire.com");
        request.setPassword("Password123!");
        request.setRole(Role.JOB_SEEKER);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("1234567890");
        request.setDateOfBirth(LocalDate.of(1995, 1, 1));

        when(userRepository.existsByEmail("seeker@revhire.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_pass");

        User savedUser = new User("seeker@revhire.com", "encoded_pass", Role.JOB_SEEKER);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");

        AuthResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(10L, response.getUserId());
        assertEquals("JOB_SEEKER", response.getRole());

        verify(jobSeekerProfileRepository, times(1)).save(any(JobSeekerProfile.class));
        verify(employerProfileRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_Employer_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("employer@corp.com");
        request.setPassword("Password123!");
        request.setRole(Role.EMPLOYER);
        request.setCompanyName("Acme Corp");
        request.setContactName("Jane Smith");
        request.setWebsite("https://acme.com");

        when(userRepository.existsByEmail("employer@corp.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_pass");

        User savedUser = new User("employer@corp.com", "encoded_pass", Role.EMPLOYER);
        savedUser.setId(20L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");

        AuthResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(20L, response.getUserId());
        assertEquals("EMPLOYER", response.getRole());

        verify(employerProfileRepository, times(1)).save(any(EmployerProfile.class));
        verify(jobSeekerProfileRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists_ThrowsException() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("duplicate@revhire.com");

        when(userRepository.existsByEmail("duplicate@revhire.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testAuthenticateUser_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@revhire.com");
        request.setPassword("Secret123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("auth.token");

        User user = new User("user@revhire.com", "hashed", Role.JOB_SEEKER);
        user.setId(5L);
        when(userRepository.findByEmail("user@revhire.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.authenticateUser(request);

        assertNotNull(response);
        assertEquals("auth.token", response.getToken());
        assertEquals(5L, response.getUserId());
        assertEquals("JOB_SEEKER", response.getRole());
    }

    @Test
    void testRegisterUser_AdminRole_ThrowsException() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@revhire.com");
        request.setPassword("Secret123!");
        request.setRole(Role.ADMIN);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.registerUser(request));
        assertEquals("Registration with role ADMIN is not permitted", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_NullRole_DefaultsToJobSeeker() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("default@revhire.com");
        request.setPassword("Secret123!");
        request.setRole(null);

        when(userRepository.existsByEmail("default@revhire.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("encoded_pass");

        User savedUser = new User("default@revhire.com", "encoded_pass", Role.JOB_SEEKER);
        savedUser.setId(20L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");

        AuthResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals(Role.JOB_SEEKER, request.getRole());
        verify(jobSeekerProfileRepository, times(1)).save(any());
    }
}
