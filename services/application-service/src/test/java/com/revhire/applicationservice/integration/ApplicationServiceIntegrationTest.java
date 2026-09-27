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

        mockMvc.perform(post("/applications")
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

        mockMvc.perform(get("/applications/" + saved.getId()))
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

        mockMvc.perform(get("/applications/user/100"))
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

        mockMvc.perform(put("/applications/" + saved.getId() + "/status")
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

        mockMvc.perform(delete("/applications/" + saved.getId()))
                .andExpect(status().isNoContent());
    }
}