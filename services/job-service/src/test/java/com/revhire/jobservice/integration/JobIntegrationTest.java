package com.revhire.jobservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revhire.jobservice.dto.request.CreateJobRequest;
import com.revhire.jobservice.entity.Job;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import com.revhire.jobservice.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
    }

    @Test
    void createAndGetJob_shouldWorkEndToEnd() {

        CreateJobRequest request = new CreateJobRequest();
        request.setTitle("Integration Java Developer");
        request.setDescription("Integration test job");
        request.setLocation("Pune");
        request.setSkills("Java, Spring Boot");
        request.setSalary(new BigDecimal("80000"));
        request.setJobType(JobType.FULL_TIME);
        request.setEmployerId(100L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateJobRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<String> createResponse =
                restTemplate.postForEntity(
                        "/api/jobs",
                        entity,
                        String.class
                );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        assertNotNull(createResponse.getBody());
        assertTrue(createResponse.getBody().contains("Integration Java Developer"));

        Job savedJob =
                jobRepository.findAll()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        ResponseEntity<String> getResponse =
                restTemplate.getForEntity(
                        "/api/jobs/" + savedJob.getId(),
                        String.class
                );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        assertNotNull(getResponse.getBody());
        assertTrue(getResponse.getBody().contains("Integration Java Developer"));
    }

    @Test
    void getNonExistingJob_shouldReturn404() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/api/jobs/999999",
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}