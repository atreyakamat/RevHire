package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.UserRegistrationRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRegistrationRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        request.setRole(Role.JOB_SEEKER);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
        
        verify(userRepository, never()).save(any(User.class));
    }
}
