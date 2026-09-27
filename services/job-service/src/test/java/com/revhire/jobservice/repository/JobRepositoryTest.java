package com.revhire.jobservice.repository;

import com.revhire.jobservice.entity.Job;
import com.revhire.jobservice.entity.JobStatus;
import com.revhire.jobservice.entity.JobType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Job createJob(
            String title,
            String location,
            String skills,
            BigDecimal salary,
            JobType jobType,
            JobStatus status,
            Long employerId) {

        Job job = new Job();

        job.setTitle(title);
        job.setDescription("Backend developer");
        job.setLocation(location);
        job.setSkills(skills);
        job.setSalary(salary);
        job.setJobType(jobType);
        job.setStatus(status);
        job.setEmployerId(employerId);

        return job;
    }

    @Test
    void save_shouldPersistJob() {

        Job job = createJob(
                "Java Developer",
                "Pune",
                "Java, Spring Boot",
                new BigDecimal("80000"),
                JobType.FULL_TIME,
                JobStatus.ACTIVE,
                100L
        );

        Job saved = jobRepository.save(job);

        assertNotNull(saved.getId());
        assertEquals("Java Developer", saved.getTitle());
    }

    @Test
    void findByLocation_shouldReturnMatchingJobs() {

        Job job = createJob(
                "Java Developer",
                "Pune",
                "Java",
                new BigDecimal("80000"),
                JobType.FULL_TIME,
                JobStatus.ACTIVE,
                100L
        );

        entityManager.persistAndFlush(job);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Job> result =
                jobRepository.findByLocationContainingIgnoreCase(
                        "pune",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("Pune",
                result.getContent().get(0).getLocation());
    }

    @Test
    void findBySkills_shouldReturnMatchingJobs() {

        Job job = createJob(
                "Java Developer",
                "Mumbai",
                "Java, Spring Boot",
                new BigDecimal("80000"),
                JobType.FULL_TIME,
                JobStatus.ACTIVE,
                100L
        );

        entityManager.persistAndFlush(job);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Job> result =
                jobRepository.findBySkillsContainingIgnoreCase(
                        "spring",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertTrue(
                result.getContent()
                        .get(0)
                        .getSkills()
                        .contains("Spring Boot")
        );
    }

    @Test
    void findBySalary_shouldReturnMatchingJobs() {

        Job job = createJob(
                "Senior Developer",
                "Pune",
                "Java",
                new BigDecimal("100000"),
                JobType.FULL_TIME,
                JobStatus.ACTIVE,
                100L
        );

        entityManager.persistAndFlush(job);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Job> result =
                jobRepository.findBySalaryGreaterThanEqual(
                        new BigDecimal("90000"),
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                new BigDecimal("100000"),
                result.getContent().get(0).getSalary()
        );
    }

    @Test
    void findByJobType_shouldReturnMatchingJobs() {

        Job job = createJob(
                "Java Developer",
                "Pune",
                "Java",
                new BigDecimal("80000"),
                JobType.FULL_TIME,
                JobStatus.ACTIVE,
                100L
        );

        entityManager.persistAndFlush(job);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Job> result =
                jobRepository.findByJobType(
                        JobType.FULL_TIME,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                JobType.FULL_TIME,
                result.getContent().get(0).getJobType()
        );
    }

    @Test
    void findByStatus_shouldReturnMatchingJobs() {

        Job job = createJob(
                "Java Developer",
                "Pune",
                "Java",
                new BigDecimal("80000"),
                JobType.FULL_TIME,
                JobStatus.ACTIVE,
                100L
        );

        entityManager.persistAndFlush(job);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Job> result =
                jobRepository.findByStatus(
                        JobStatus.ACTIVE,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                JobStatus.ACTIVE,
                result.getContent().get(0).getStatus()
        );
    }

    @Test
    void findByEmployerId_shouldReturnEmployerJobs() {

        Job job = createJob(
                "Java Developer",
                "Pune",
                "Java",
                new BigDecimal("80000"),
                JobType.FULL_TIME,
                JobStatus.ACTIVE,
                100L
        );

        entityManager.persistAndFlush(job);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Job> result =
                jobRepository.findByEmployerId(
                        100L,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                100L,
                result.getContent().get(0).getEmployerId()
        );
    }
}