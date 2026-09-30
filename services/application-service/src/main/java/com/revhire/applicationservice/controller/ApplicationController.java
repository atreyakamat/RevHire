package com.revhire.applicationservice.controller;

import com.revhire.applicationservice.client.JobClient;
import com.revhire.applicationservice.client.UserClient;
import com.revhire.applicationservice.dto.request.ApplicationRequest;
import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.dto.response.JobResponse;
import com.revhire.applicationservice.dto.response.UserProfileResponse;
import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.mapper.ApplicationMapper;
import com.revhire.applicationservice.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobClient jobClient;
    private final UserClient userClient;

    public ApplicationController(
            ApplicationService applicationService,
            JobClient jobClient) {
        this(applicationService, jobClient, null);
    }

    @Autowired
    public ApplicationController(
            ApplicationService applicationService,
            JobClient jobClient,
            @Autowired(required = false) UserClient userClient) {

        this.applicationService = applicationService;
        this.jobClient = jobClient;
        this.userClient = userClient;
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private boolean isCallerAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    private boolean isCallerEmployer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"));
        }
        return false;
    }

    private boolean isCallerJobSeeker() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"));
        }
        return false;
    }

    private void verifyEmployerJobOwnership(Long jobId, Long authUserId, String denialMessage) {
        if (jobClient == null) {
            return;
        }
        try {
            JobResponse job = jobClient.getJobById(jobId);
            if (job != null && !job.getEmployerId().equals(authUserId)) {
                throw new AccessDeniedException(denialMessage);
            }
        } catch (AccessDeniedException ade) {
            throw ade;
        } catch (Exception ignored) {
            // Graceful degradation when job service is unreachable
        }
    }

    private void verifyCanViewApplication(Application application, Long authUserId) {
        if (authUserId == null || isCallerAdmin()) {
            return;
        }
        if (isCallerJobSeeker() && !application.getUserId().equals(authUserId)) {
            throw new AccessDeniedException("You are not authorized to view this application");
        }
        if (isCallerEmployer()) {
            verifyEmployerJobOwnership(application.getJobId(), authUserId,
                    "You are not authorized to view applications for another employer's job");
        }
    }

    private void verifyCanViewApplicationsByJob(Long jobId, Long authUserId) {
        if (authUserId == null || isCallerAdmin()) {
            return;
        }
        if (isCallerJobSeeker()) {
            throw new AccessDeniedException("Job seekers cannot view applications by job");
        }
        if (isCallerEmployer()) {
            verifyEmployerJobOwnership(jobId, authUserId,
                    "You can only view applications for your own jobs");
        }
    }

    // Submit a new application
    @PostMapping
    public ResponseEntity<ApplicationResponse> submitApplication(
            @RequestBody ApplicationRequest request) {

        Application application = ApplicationMapper.toEntity(request);
        Long authUserId = getAuthenticatedUserId();
        if (authUserId != null) {
            application.setUserId(authUserId);
        }

        if (jobClient != null && application.getJobId() != null) {
            try {
                JobResponse job = jobClient.getJobById(application.getJobId());
                if (job == null) {
                    throw new IllegalArgumentException("Job with id " + application.getJobId() + " does not exist");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception ignored) {
                // Graceful degradation when job service is unreachable
            }
        }

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

        Long authUserId = getAuthenticatedUserId();
        verifyCanViewApplication(application, authUserId);

        return ResponseEntity.ok(
                ApplicationMapper.toResponse(application)
        );
    }

    // Get all applications
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {

        Long authUserId = getAuthenticatedUserId();
        if (authUserId != null && isCallerJobSeeker() && !isCallerAdmin()) {
            List<ApplicationResponse> responses =
                    applicationService.getApplicationsByUser(authUserId)
                            .stream()
                            .map(ApplicationMapper::toResponse)
                            .toList();
            return ResponseEntity.ok(responses);
        }

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

        Long authUserId = getAuthenticatedUserId();
        if (authUserId != null && !isCallerAdmin() && !authUserId.equals(userId)) {
            throw new AccessDeniedException("You can only view your own applications");
        }

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

        Long authUserId = getAuthenticatedUserId();
        verifyCanViewApplicationsByJob(jobId, authUserId);

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !isCallerAdmin()) {
            throw new AccessDeniedException("Only admins can view applications by status across all users");
        }

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

    // Test Feign communication with User Service
    @GetMapping("/user-details/{userId}")
    public ResponseEntity<UserProfileResponse> getUserFromUserService(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("userId") Long userId) {

        if (userClient == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok(
                userClient.getUserById(authHeader, userId)
        );
    }

    // Update application status
    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") ApplicationStatus status) {

        Long authUserId = getAuthenticatedUserId();
        if (authUserId != null && !isCallerAdmin() && isCallerEmployer()) {
            Application application = applicationService.getApplicationById(id);
            verifyEmployerJobOwnership(application.getJobId(), authUserId,
                    "You cannot update application status for another employer's job");
        }

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

        Long authUserId = getAuthenticatedUserId();
        if (authUserId != null && !isCallerAdmin() && isCallerJobSeeker()) {
            Application application = applicationService.getApplicationById(id);
            if (!application.getUserId().equals(authUserId)) {
                throw new AccessDeniedException("You cannot withdraw another user's application");
            }
        }

        applicationService.deleteApplication(id);

        return ResponseEntity.noContent().build();
    }
}