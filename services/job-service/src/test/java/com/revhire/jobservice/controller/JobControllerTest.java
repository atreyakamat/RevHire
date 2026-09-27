package com.revhire.jobservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revhire.jobservice.dto.request.CreateJobRequest;
import com.revhire.jobservice.dto.request.UpdateJobRequest;
import com.revhire.jobservice.dto.response.JobResponse;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import com.revhire.jobservice.exception.GlobalExceptionHandler;
import com.revhire.jobservice.exception.JobNotFoundException;
import com.revhire.jobservice.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@Import(GlobalExceptionHandler.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    private JobResponse createResponse() {

        JobResponse response = new JobResponse();

        response.setId(1L);
        response.setTitle("Java Developer");
        response.setDescription("Java backend developer");
        response.setLocation("Pune");
        response.setSkills("Java, Spring Boot");
        response.setSalary(new BigDecimal("80000"));
        response.setJobType(JobType.FULL_TIME);
        response.setStatus(JobStatus.DRAFT);
        response.setEmployerId(100L);

        return response;
    }

    private CreateJobRequest createRequest() {

        CreateJobRequest request = new CreateJobRequest();

        request.setTitle("Java Developer");
        request.setDescription("Java backend developer");
        request.setLocation("Pune");
        request.setSkills("Java, Spring Boot");
        request.setSalary(new BigDecimal("80000"));
        request.setJobType(JobType.FULL_TIME);
        request.setEmployerId(100L);

        return request;
    }

    private UpdateJobRequest updateRequest() {

        UpdateJobRequest request = new UpdateJobRequest();

        request.setTitle("Senior Java Developer");
        request.setDescription("Senior backend developer");
        request.setLocation("Mumbai");
        request.setSkills("Java, Spring Boot, Microservices");
        request.setSalary(new BigDecimal("100000"));
        request.setJobType(JobType.FULL_TIME);
        request.setStatus(JobStatus.ACTIVE);

        return request;
    }

    @Test
    void createJob_shouldReturn201() throws Exception {

        JobResponse response = createResponse();

        when(jobService.createJob(any(CreateJobRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"));

        verify(jobService).createJob(any(CreateJobRequest.class));
    }

    @Test
    void createJob_shouldReturn400_whenValidationFails() throws Exception {

        CreateJobRequest request = createRequest();

        request.setTitle("");
        request.setLocation("");

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jobService);
    }

    @Test
    void getJob_shouldReturn200() throws Exception {

        when(jobService.getJobById(1L))
                .thenReturn(createResponse());

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"));

        verify(jobService).getJobById(1L);
    }

    @Test
    void getJob_shouldReturn404_whenJobDoesNotExist() throws Exception {

        when(jobService.getJobById(99L))
                .thenThrow(new JobNotFoundException(99L));

        mockMvc.perform(get("/api/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Job not found with id: 99"));

        verify(jobService).getJobById(99L);
    }

    @Test
    void getJobs_shouldReturn200_withPagination() throws Exception {

        JobResponse response = createResponse();

        PageImpl<JobResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        when(jobService.searchJobs(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/jobs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title")
                        .value("Java Developer"))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));

        verify(jobService).searchJobs(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        );
    }

    @Test
    void getJobs_shouldPassSearchFilters() throws Exception {

        JobResponse response = createResponse();

        PageImpl<JobResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        when(jobService.searchJobs(
                eq("Pune"),
                eq("Java"),
                eq(new BigDecimal("70000")),
                eq(JobType.FULL_TIME),
                eq(JobStatus.ACTIVE),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/jobs")
                        .param("location", "Pune")
                        .param("skills", "Java")
                        .param("minSalary", "70000")
                        .param("jobType", "FULL_TIME")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements")
                        .value(1));

        verify(jobService).searchJobs(
                eq("Pune"),
                eq("Java"),
                eq(new BigDecimal("70000")),
                eq(JobType.FULL_TIME),
                eq(JobStatus.ACTIVE),
                any()
        );
    }

    @Test
    void updateJob_shouldReturn200() throws Exception {

        when(jobService.updateJob(
                eq(1L),
                any(UpdateJobRequest.class)
        )).thenReturn(createResponse());

        mockMvc.perform(put("/api/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                updateRequest()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Java Developer"));

        verify(jobService).updateJob(
                eq(1L),
                any(UpdateJobRequest.class)
        );
    }

    @Test
    void deleteJob_shouldReturn204() throws Exception {

        doNothing().when(jobService).deleteJob(1L);

        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isNoContent());

        verify(jobService).deleteJob(1L);
    }

    @Test
    void getEmployerJobs_shouldReturn200() throws Exception {

        JobResponse response = createResponse();

        PageImpl<JobResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        when(jobService.getJobsByEmployer(
                eq(100L),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/jobs/employer/100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].employerId")
                        .value(100))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));

        verify(jobService).getJobsByEmployer(
                eq(100L),
                any()
        );
    }
}