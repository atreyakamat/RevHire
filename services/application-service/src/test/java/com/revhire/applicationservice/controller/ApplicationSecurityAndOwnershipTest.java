package com.revhire.applicationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revhire.applicationservice.client.JobClient;
import com.revhire.applicationservice.config.SecurityConfig;
import com.revhire.applicationservice.dto.request.ApplicationRequest;
import com.revhire.applicationservice.dto.response.JobResponse;
import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.exception.GlobalExceptionHandler;
import com.revhire.applicationservice.security.JwtAuthenticationEntryPoint;
import com.revhire.applicationservice.security.JwtTokenProvider;
import com.revhire.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class ApplicationSecurityAndOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private JobClient jobClient;

    @MockitoBean
    private com.revhire.applicationservice.client.UserClient userClient;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private Application application;
    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        application = new Application();
        application.setId(1L);
        application.setJobId(10L);
        application.setUserId(100L); // owned by seeker 100
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        jobResponse = new JobResponse();
        jobResponse.setId(10L);
        jobResponse.setEmployerId(200L); // employer 200 owns job 10
    }

    @Test
    void testSubmitApplication_WithoutToken_Returns401() throws Exception {
        ApplicationRequest req = new ApplicationRequest();
        req.setJobId(10L);
        req.setUserId(999L);

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSubmitApplication_DerivesUserIdFromToken_OverridesForgedBody() throws Exception {
        String token = "seeker.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(100L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("JOB_SEEKER");

        when(jobClient.getJobById(10L)).thenReturn(jobResponse);
        when(applicationService.submitApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationRequest req = new ApplicationRequest();
        req.setJobId(10L);
        req.setUserId(999L); // forged id

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(100)); // verified overridden to 100
    }

    @Test
    void testGetApplicationById_OtherSeeker_Returns403() throws Exception {
        String token = "other.seeker.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(101L); // other seeker
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("JOB_SEEKER");
        when(applicationService.getApplicationById(1L)).thenReturn(application);

        mockMvc.perform(get("/applications/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetApplicationById_OwnerSeeker_Returns200() throws Exception {
        String token = "owner.seeker.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(100L); // owner
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("JOB_SEEKER");
        when(applicationService.getApplicationById(1L)).thenReturn(application);

        mockMvc.perform(get("/applications/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(100));
    }

    @Test
    void testGetApplicationById_OtherEmployer_Returns403() throws Exception {
        String token = "other.employer.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(201L); // other employer
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(applicationService.getApplicationById(1L)).thenReturn(application);
        when(jobClient.getJobById(10L)).thenReturn(jobResponse); // job owned by 200

        mockMvc.perform(get("/applications/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetApplicationById_JobOwnerEmployer_Returns200() throws Exception {
        String token = "owner.employer.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(200L); // employer 200 owns job 10
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(applicationService.getApplicationById(1L)).thenReturn(application);
        when(jobClient.getJobById(10L)).thenReturn(jobResponse);

        mockMvc.perform(get("/applications/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateStatus_OtherEmployer_Returns403() throws Exception {
        String token = "other.employer.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(201L); // other employer
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(applicationService.getApplicationById(1L)).thenReturn(application);
        when(jobClient.getJobById(10L)).thenReturn(jobResponse);

        mockMvc.perform(put("/applications/1/status")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "SHORTLISTED"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteApplication_OtherSeeker_Returns403() throws Exception {
        String token = "other.seeker.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(101L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("JOB_SEEKER");
        when(applicationService.getApplicationById(1L)).thenReturn(application);

        mockMvc.perform(delete("/applications/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteApplication_OwnerSeeker_Returns204() throws Exception {
        String token = "owner.seeker.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(100L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("JOB_SEEKER");
        when(applicationService.getApplicationById(1L)).thenReturn(application);

        mockMvc.perform(delete("/applications/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSeekerGetApplicationsByJob_Returns403() throws Exception {
        String token = "seeker.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(100L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("JOB_SEEKER");

        mockMvc.perform(get("/applications/job/10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testNonAdminGetApplicationsByStatus_Returns403() throws Exception {
        String token = "employer.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(200L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");

        mockMvc.perform(get("/applications/status/APPLIED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminGetApplicationsByStatus_Returns200() throws Exception {
        String token = "admin.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("ADMIN");
        when(applicationService.getApplicationsByStatus(ApplicationStatus.APPLIED))
                .thenReturn(java.util.List.of(application));

        mockMvc.perform(get("/applications/status/APPLIED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
