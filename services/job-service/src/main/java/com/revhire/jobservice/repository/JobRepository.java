package com.revhire.jobservice.repository;

import com.revhire.jobservice.entity.Job;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByLocationContainingIgnoreCase(
            String location,
            Pageable pageable
    );

    Page<Job> findBySkillsContainingIgnoreCase(
            String skills,
            Pageable pageable
    );

    Page<Job> findBySalaryGreaterThanEqual(
            java.math.BigDecimal salary,
            Pageable pageable
    );

    Page<Job> findByJobType(
            JobType jobType,
            Pageable pageable
    );

    Page<Job> findByStatus(
            JobStatus status,
            Pageable pageable
    );

    Page<Job> findByEmployerId(
            Long employerId,
            Pageable pageable
    );
}