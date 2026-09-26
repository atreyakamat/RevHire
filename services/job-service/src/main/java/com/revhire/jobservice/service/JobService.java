package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.request.CreateJobRequest;
import com.revhire.jobservice.dto.request.UpdateJobRequest;
import com.revhire.jobservice.dto.response.JobResponse;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {

    JobResponse createJob(CreateJobRequest request);

    JobResponse getJobById(Long id);

    Page<JobResponse> getAllJobs(Pageable pageable);

    JobResponse updateJob(Long id, UpdateJobRequest request);

    void deleteJob(Long id);

    Page<JobResponse> searchJobs(
            String location,
            String skills,
            java.math.BigDecimal minSalary,
            JobType jobType,
            JobStatus status,
            Pageable pageable
    );

    Page<JobResponse> getJobsByEmployer(
            Long employerId,
            Pageable pageable
    );
}