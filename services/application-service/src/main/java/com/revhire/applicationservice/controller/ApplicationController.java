package com.revhire.applicationservice.controller;

import com.revhire.applicationservice.client.JobClient;
import com.revhire.applicationservice.dto.request.ApplicationRequest;
import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.dto.response.JobResponse;
import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.mapper.ApplicationMapper;
import com.revhire.applicationservice.service.ApplicationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobClient jobClient;

    public ApplicationController(
            ApplicationService applicationService,
            JobClient jobClient) {

        this.applicationService = applicationService;
        this.jobClient = jobClient;
    }

    // Submit a new application
    @PostMapping
    public ResponseEntity<ApplicationResponse> submitApplication(
            @RequestBody ApplicationRequest request) {

        Application application =
                ApplicationMapper.toEntity(request);

        Application savedApplication =
                applicationService.submitApplication(application);

        return new ResponseEntity<>(
                ApplicationMapper.toResponse(savedApplication),
                HttpStatus.CREATED
        );
    }

    // Get application by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable("id") Long id) {

        Application application =
                applicationService.getApplicationById(id);

        return ResponseEntity.ok(
                ApplicationMapper.toResponse(application)
        );
    }

    // Get all applications
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {

        List<ApplicationResponse> responses =
                applicationService.getAllApplications()
                        .stream()
                        .map(ApplicationMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // Get applications by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByUser(
            @PathVariable("userId") Long userId) {

        List<ApplicationResponse> responses =
                applicationService.getApplicationsByUser(userId)
                        .stream()
                        .map(ApplicationMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // Get applications by job
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByJob(
            @PathVariable("jobId") Long jobId) {

        List<ApplicationResponse> responses =
                applicationService.getApplicationsByJob(jobId)
                        .stream()
                        .map(ApplicationMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // Get applications by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByStatus(
            @PathVariable("status") ApplicationStatus status) {

        List<ApplicationResponse> responses =
                applicationService.getApplicationsByStatus(status)
                        .stream()
                        .map(ApplicationMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // Test Feign communication with Job Service
    @GetMapping("/job-details/{jobId}")
    public ResponseEntity<JobResponse> getJobFromJobService(
            @PathVariable("jobId") Long jobId) {

        return ResponseEntity.ok(
                jobClient.getJobById(jobId)
        );
    }

    // Update application status
    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") ApplicationStatus status) {

        Application application =
                applicationService.updateApplicationStatus(id, status);

        return ResponseEntity.ok(
                ApplicationMapper.toResponse(application)
        );
    }

    // Delete application
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable("id") Long id) {

        applicationService.deleteApplication(id);

        return ResponseEntity.noContent().build();
    }
}