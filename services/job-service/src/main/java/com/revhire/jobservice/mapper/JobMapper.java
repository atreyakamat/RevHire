package com.revhire.jobservice.mapper;

import com.revhire.jobservice.dto.request.CreateJobRequest;
import com.revhire.jobservice.dto.request.UpdateJobRequest;
import com.revhire.jobservice.dto.response.JobResponse;
import com.revhire.jobservice.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public Job toEntity(CreateJobRequest request) {
        Job job = new Job();

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSkills(request.getSkills());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setEmployerId(request.getEmployerId());

        return job;
    }

    public void updateEntity(Job job, UpdateJobRequest request) {
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSkills(request.getSkills());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setStatus(request.getStatus());
    }

    public JobResponse toResponse(Job job) {
        JobResponse response = new JobResponse();

        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setSkills(job.getSkills());
        response.setSalary(job.getSalary());
        response.setJobType(job.getJobType());
        response.setStatus(job.getStatus());
        response.setEmployerId(job.getEmployerId());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());

        return response;
    }
}