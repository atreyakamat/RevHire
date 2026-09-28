package com.revhire.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.revhire.userservice.dto.request.LoginRequest;
import com.revhire.userservice.dto.request.UserRegistrationRequest;
import com.revhire.userservice.dto.response.AuthResponse;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.exception.GlobalExceptionHandler;
import com.revhire.userservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newuser@revhire.com");
        request.setPassword("SecretPass123!");
        request.setRole(Role.JOB_SEEKER);
        request.setFirstName("First");
        request.setLastName("Last");

        AuthResponse authResponse = new AuthResponse("mock.jwt.token", 1L, "JOB_SEEKER");
        when(authService.registerUser(any(UserRegistrationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.role").value("JOB_SEEKER"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@revhire.com");
        request.setPassword("SecretPass123!");
        AuthResponse authResponse = new AuthResponse("mock.login.token", 2L, "EMPLOYER");
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.login.token"))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.role").value("EMPLOYER"));
    }
}
