package com.revhire.jobservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revhire.jobservice.config.SecurityConfig;
import com.revhire.jobservice.dto.request.CreateJobRequest;
import com.revhire.jobservice.dto.request.UpdateJobRequest;
import com.revhire.jobservice.dto.response.JobResponse;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import com.revhire.jobservice.exception.GlobalExceptionHandler;
import com.revhire.jobservice.security.JwtAuthenticationEntryPoint;
import com.revhire.jobservice.security.JwtTokenProvider;
import com.revhire.jobservice.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class JobSecurityAndOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private CreateJobRequest createRequest;
    private UpdateJobRequest updateRequest;
    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateJobRequest();
        createRequest.setTitle("Software Engineer");
        createRequest.setDescription("Backend Java role");
        createRequest.setLocation("New York");
        createRequest.setSkills("Java, Spring");
        createRequest.setSalary(BigDecimal.valueOf(120000));
        createRequest.setJobType(JobType.FULL_TIME);
        createRequest.setEmployerId(999L); // forged id in body

        updateRequest = new UpdateJobRequest();
        updateRequest.setTitle("Senior Software Engineer");
        updateRequest.setDescription("Updated desc");
        updateRequest.setLocation("Remote");
        updateRequest.setSkills("Java, Spring, AWS");
        updateRequest.setSalary(BigDecimal.valueOf(140000));
        updateRequest.setJobType(JobType.FULL_TIME);
        updateRequest.setStatus(JobStatus.ACTIVE);

        jobResponse = new JobResponse();
        jobResponse.setId(1L);
        jobResponse.setEmployerId(10L); // owned by employer 10
        jobResponse.setTitle("Software Engineer");
    }

    @Test
    void testPublicJobSearch_AllowedWithoutToken() throws Exception {
        when(jobService.searchJobs(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(jobResponse)));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateJob_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateJob_WrongRoleJobSeeker_Returns403() throws Exception {
        String token = "seeker.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(5L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("JOB_SEEKER");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateJob_EmployerRole_SuccessAndDerivesEmployerId() throws Exception {
        String token = "employer.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(10L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(jobService.createJob(any())).thenReturn(jobResponse);

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdateJob_WrongOwnerEmployer_Returns403() throws Exception {
        String token = "employer2.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(20L); // employer 20, but job belongs to 10
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(jobService.getJobById(1L)).thenReturn(jobResponse);

        mockMvc.perform(put("/api/jobs/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateJob_CorrectOwnerEmployer_Returns200() throws Exception {
        String token = "employer1.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(10L); // employer 10 owns the job
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(jobService.getJobById(1L)).thenReturn(jobResponse);
        when(jobService.updateJob(eq(1L), any())).thenReturn(jobResponse);

        mockMvc.perform(put("/api/jobs/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteJob_WrongOwnerEmployer_Returns403() throws Exception {
        String token = "employer2.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(20L); // employer 20
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(jobService.getJobById(1L)).thenReturn(jobResponse);

        mockMvc.perform(delete("/api/jobs/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteJob_Admin_Allowed() throws Exception {
        String token = "admin.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromJWT(token)).thenReturn(99L);
        when(jwtTokenProvider.getRoleFromJWT(token)).thenReturn("ADMIN");

        mockMvc.perform(delete("/api/jobs/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
