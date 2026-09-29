package com.revhire.applicationservice.integration;

import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private com.revhire.applicationservice.security.JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
    }

    @Test
    void submitApplication_shouldReturn201() throws Exception {

        String request = """
                {
                    "jobId": 1,
                    "userId": 100,
                    "resumeId": 10
                }
                """;

        String token = tokenProvider.generateToken(100L, "JOB_SEEKER", "seeker100@test.com");

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.resumeId").value(10))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void getApplicationById_shouldReturn200() throws Exception {

        Application application = new Application();

        application.setJobId(1L);
        application.setUserId(100L);
        application.setResumeId(10L);
        application.setStatus(ApplicationStatus.APPLIED);

        Application saved = applicationRepository.save(application);
        String token = tokenProvider.generateToken(100L, "JOB_SEEKER", "seeker100@test.com");

        mockMvc.perform(get("/applications/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.resumeId").value(10))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void getApplicationsByUser_shouldReturn200() throws Exception {

        Application application = new Application();

        application.setJobId(1L);
        application.setUserId(100L);
        application.setResumeId(10L);
        application.setStatus(ApplicationStatus.APPLIED);

        applicationRepository.save(application);
        String token = tokenProvider.generateToken(100L, "JOB_SEEKER", "seeker100@test.com");

        mockMvc.perform(get("/applications/user/100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(100))
                .andExpect(jsonPath("$[0].jobId").value(1));
    }

    @Test
    void updateApplicationStatus_shouldReturn200() throws Exception {

        Application application = new Application();

        application.setJobId(1L);
        application.setUserId(100L);
        application.setResumeId(10L);
        application.setStatus(ApplicationStatus.APPLIED);

        Application saved = applicationRepository.save(application);
        String adminToken = tokenProvider.generateToken(999L, "ADMIN", "admin@test.com");

        mockMvc.perform(put("/applications/" + saved.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "SHORTLISTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void deleteApplication_shouldReturn204() throws Exception {

        Application application = new Application();

        application.setJobId(1L);
        application.setUserId(100L);
        application.setResumeId(10L);
        application.setStatus(ApplicationStatus.APPLIED);

        Application saved = applicationRepository.save(application);
        String token = tokenProvider.generateToken(100L, "JOB_SEEKER", "seeker100@test.com");

        mockMvc.perform(delete("/applications/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void submitApplication_withSpoofedHeadersWithoutJwt_shouldReturn401() throws Exception {
        String request = """
                {
                    "jobId": 1,
                    "userId": 100,
                    "resumeId": 10
                }
                """;

        mockMvc.perform(post("/applications")
                        .header("X-User-Id", "100")
                        .header("X-User-Role", "JOB_SEEKER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }
}