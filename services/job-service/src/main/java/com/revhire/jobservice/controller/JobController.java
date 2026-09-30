package com.revhire.jobservice.controller;

import com.revhire.jobservice.dto.request.CreateJobRequest;
import com.revhire.jobservice.dto.request.UpdateJobRequest;
import com.revhire.jobservice.dto.response.JobResponse;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import com.revhire.jobservice.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
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

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request) {

        Long employerId = getAuthenticatedUserId();
        if (employerId != null) {
            request.setEmployerId(employerId);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobService.createJob(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(
                jobService.getJobById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getJobs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "skills", required = false) String skills,
            @RequestParam(name = "minSalary", required = false) BigDecimal minSalary,
            @RequestParam(name = "jobType", required = false) JobType jobType,
            @RequestParam(name = "status", required = false) JobStatus status) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                jobService.searchJobs(
                        location,
                        skills,
                        minSalary,
                        jobType,
                        status,
                        pageable
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateJobRequest request) {

        Long authenticatedUserId = getAuthenticatedUserId();
        if (authenticatedUserId != null) {
            JobResponse existingJob = jobService.getJobById(id);
            if (!isCallerAdmin() && !existingJob.getEmployerId().equals(authenticatedUserId)) {
                throw new AccessDeniedException("You are not authorized to update this job");
            }
        }

        return ResponseEntity.ok(
                jobService.updateJob(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable("id") Long id) {

        Long authenticatedUserId = getAuthenticatedUserId();
        if (authenticatedUserId != null) {
            JobResponse existingJob = jobService.getJobById(id);
            if (!isCallerAdmin() && !existingJob.getEmployerId().equals(authenticatedUserId)) {
                throw new AccessDeniedException("You are not authorized to delete this job");
            }
        }

        jobService.deleteJob(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employer/{employerId}")
    public ResponseEntity<Page<JobResponse>> getEmployerJobs(
            @PathVariable("employerId") Long employerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Long authenticatedUserId = getAuthenticatedUserId();
        if (authenticatedUserId != null && !isCallerAdmin() && !employerId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You can only view your own employer job postings");
        }

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                jobService.getJobsByEmployer(
                        employerId,
                        pageable
                )
        );
    }
}