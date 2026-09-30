package com.revhire.applicationservice.controller;

import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private com.revhire.applicationservice.client.JobClient jobClient;

    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {

        ApplicationController controller =
                new ApplicationController(applicationService, jobClient);

        lenient().when(jobClient.getJobById(any())).thenReturn(new com.revhire.applicationservice.dto.response.JobResponse());

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }


    private Application createApplication() {

        Application application = new Application();

        application.setId(1L);
        application.setJobId(101L);
        application.setUserId(201L);
        application.setResumeId(301L);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        return application;
    }


    @Test
    void submitApplication_shouldReturnCreated() throws Exception {

        Application application = createApplication();

        when(applicationService.submitApplication(any(Application.class)))
                .thenReturn(application);

        mockMvc.perform(
                        post("/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "jobId": 101,
                                            "userId": 201,
                                            "resumeId": 301
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.jobId").value(101))
                .andExpect(jsonPath("$.userId").value(201))
                .andExpect(jsonPath("$.resumeId").value(301))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        verify(applicationService)
                .submitApplication(any(Application.class));
    }


    @Test
    void getApplicationById_shouldReturnApplication() throws Exception {

        Application application = createApplication();

        when(applicationService.getApplicationById(1L))
                .thenReturn(application);

        mockMvc.perform(
                        get("/applications/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.jobId").value(101))
                .andExpect(jsonPath("$.userId").value(201))
                .andExpect(jsonPath("$.resumeId").value(301))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        verify(applicationService).getApplicationById(1L);
    }


    @Test
    void getAllApplications_shouldReturnApplications() throws Exception {

        Application application = createApplication();

        when(applicationService.getAllApplications())
                .thenReturn(List.of(application));

        mockMvc.perform(
                        get("/applications")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(applicationService).getAllApplications();
    }


    @Test
    void getApplicationsByUser_shouldReturnApplications() throws Exception {

        Application application = createApplication();

        when(applicationService.getApplicationsByUser(201L))
                .thenReturn(List.of(application));

        mockMvc.perform(
                        get("/applications/user/201")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(201));

        verify(applicationService).getApplicationsByUser(201L);
    }


    @Test
    void getApplicationsByJob_shouldReturnApplications() throws Exception {

        Application application = createApplication();

        when(applicationService.getApplicationsByJob(101L))
                .thenReturn(List.of(application));

        mockMvc.perform(
                        get("/applications/job/101")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobId").value(101));

        verify(applicationService).getApplicationsByJob(101L);
    }


    @Test
    void getApplicationsByStatus_shouldReturnApplications() throws Exception {

        Application application = createApplication();

        when(applicationService.getApplicationsByStatus(
                ApplicationStatus.APPLIED))
                .thenReturn(List.of(application));

        mockMvc.perform(
                        get("/applications/status/APPLIED")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));

        verify(applicationService)
                .getApplicationsByStatus(ApplicationStatus.APPLIED);
    }


    @Test
    void updateApplicationStatus_shouldReturnUpdatedApplication()
            throws Exception {

        Application application = createApplication();
        application.setStatus(ApplicationStatus.SHORTLISTED);

        when(applicationService.updateApplicationStatus(
                1L,
                ApplicationStatus.SHORTLISTED))
                .thenReturn(application);

        mockMvc.perform(
                        put("/applications/1/status")
                                .param("status", "SHORTLISTED")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        verify(applicationService)
                .updateApplicationStatus(
                        1L,
                        ApplicationStatus.SHORTLISTED
                );
    }


    @Test
    void deleteApplication_shouldReturnNoContent() throws Exception {

        doNothing()
                .when(applicationService)
                .deleteApplication(1L);

        mockMvc.perform(
                        delete("/applications/1")
                )
                .andExpect(status().isNoContent());

        verify(applicationService)
                .deleteApplication(1L);
    }
}