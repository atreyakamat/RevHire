package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.request.CreateJobRequest;
import com.revhire.jobservice.dto.request.UpdateJobRequest;
import com.revhire.jobservice.dto.response.JobResponse;
import com.revhire.jobservice.entity.Job;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import com.revhire.jobservice.exception.JobNotFoundException;
import com.revhire.jobservice.mapper.JobMapper;
import com.revhire.jobservice.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobServiceImpl(
            JobRepository jobRepository,
            JobMapper jobMapper) {

        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    @Override
    public JobResponse createJob(CreateJobRequest request) {

        Job job = jobMapper.toEntity(request);

        job.setStatus(JobStatus.DRAFT);

        Job savedJob = jobRepository.save(job);

        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getAllJobs(Pageable pageable) {

        return jobRepository.findAll(pageable)
                .map(jobMapper::toResponse);
    }

    @Override
    public JobResponse updateJob(
            Long id,
            UpdateJobRequest request) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        jobMapper.updateEntity(job, request);

        Job updatedJob = jobRepository.save(job);

        return jobMapper.toResponse(updatedJob);
    }

    @Override
    public void deleteJob(Long id) {

        if (!jobRepository.existsById(id)) {
            throw new JobNotFoundException(id);
        }

        jobRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(
            String location,
            String skills,
            BigDecimal minSalary,
            JobType jobType,
            JobStatus status,
            Pageable pageable) {

        if (location != null && !location.isBlank()) {
            return jobRepository
                    .findByLocationContainingIgnoreCase(
                            location,
                            pageable)
                    .map(jobMapper::toResponse);
        }

        if (skills != null && !skills.isBlank()) {
            return jobRepository
                    .findBySkillsContainingIgnoreCase(
                            skills,
                            pageable)
                    .map(jobMapper::toResponse);
        }

        if (minSalary != null) {
            return jobRepository
                    .findBySalaryGreaterThanEqual(
                            minSalary,
                            pageable)
                    .map(jobMapper::toResponse);
        }

        if (jobType != null) {
            return jobRepository
                    .findByJobType(jobType, pageable)
                    .map(jobMapper::toResponse);
        }

        if (status != null) {
            return jobRepository
                    .findByStatus(status, pageable)
                    .map(jobMapper::toResponse);
        }

        return jobRepository
                .findAll(pageable)
                .map(jobMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByEmployer(
            Long employerId,
            Pageable pageable) {

        return jobRepository
                .findByEmployerId(employerId, pageable)
                .map(jobMapper::toResponse);
    }
}