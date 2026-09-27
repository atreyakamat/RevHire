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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private Job job;
    private JobResponse jobResponse;
    private CreateJobRequest createRequest;
    private UpdateJobRequest updateRequest;

    @BeforeEach
    void setUp() {

        job = new Job();
        job.setId(1L);
        job.setTitle("Java Developer");
        job.setDescription("Java backend developer");
        job.setLocation("Pune");
        job.setSkills("Java, Spring Boot");
        job.setSalary(new BigDecimal("80000"));
        job.setJobType(JobType.FULL_TIME);
        job.setStatus(JobStatus.DRAFT);
        job.setEmployerId(100L);

        jobResponse = new JobResponse();
        jobResponse.setId(1L);
        jobResponse.setTitle("Java Developer");
        jobResponse.setLocation("Pune");
        jobResponse.setSalary(new BigDecimal("80000"));
        jobResponse.setJobType(JobType.FULL_TIME);
        jobResponse.setStatus(JobStatus.DRAFT);
        jobResponse.setEmployerId(100L);

        createRequest = new CreateJobRequest();
        createRequest.setTitle("Java Developer");
        createRequest.setDescription("Java backend developer");
        createRequest.setLocation("Pune");
        createRequest.setSkills("Java, Spring Boot");
        createRequest.setSalary(new BigDecimal("80000"));
        createRequest.setJobType(JobType.FULL_TIME);
        createRequest.setEmployerId(100L);

        updateRequest = new UpdateJobRequest();
        updateRequest.setTitle("Senior Java Developer");
        updateRequest.setDescription("Senior backend developer");
        updateRequest.setLocation("Mumbai");
        updateRequest.setSkills("Java, Spring Boot, Microservices");
        updateRequest.setSalary(new BigDecimal("100000"));
        updateRequest.setJobType(JobType.FULL_TIME);
        updateRequest.setStatus(JobStatus.ACTIVE);
    }

    @Test
    void createJob_shouldCreateJobSuccessfully() {

        when(jobMapper.toEntity(createRequest)).thenReturn(job);
        when(jobRepository.save(job)).thenReturn(job);
        when(jobMapper.toResponse(job)).thenReturn(jobResponse);

        JobResponse result = jobService.createJob(createRequest);

        assertNotNull(result);
        assertEquals("Java Developer", result.getTitle());
        assertEquals(JobStatus.DRAFT, result.getStatus());

        verify(jobMapper).toEntity(createRequest);
        verify(jobRepository).save(job);
        verify(jobMapper).toResponse(job);
    }

    @Test
    void getJobById_shouldReturnJob_whenJobExists() {

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        JobResponse result = jobService.getJobById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java Developer", result.getTitle());

        verify(jobRepository).findById(1L);
        verify(jobMapper).toResponse(job);
    }

    @Test
    void getJobById_shouldThrowException_whenJobDoesNotExist() {

        when(jobRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                JobNotFoundException.class,
                () -> jobService.getJobById(99L)
        );

        verify(jobRepository).findById(99L);
        verify(jobMapper, never()).toResponse(any());
    }

    @Test
    void getAllJobs_shouldReturnPaginatedJobs() {

        PageRequest pageable = PageRequest.of(0, 5);

        Page<Job> jobPage =
                new PageImpl<>(List.of(job), pageable, 1);

        when(jobRepository.findAll(pageable))
                .thenReturn(jobPage);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        Page<JobResponse> result =
                jobService.getAllJobs(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Developer",
                result.getContent().get(0).getTitle());

        verify(jobRepository).findAll(pageable);
    }

    @Test
    void updateJob_shouldUpdateJobSuccessfully() {

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        when(jobRepository.save(job))
                .thenReturn(job);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        JobResponse result =
                jobService.updateJob(1L, updateRequest);

        assertNotNull(result);

        verify(jobRepository).findById(1L);
        verify(jobMapper).updateEntity(job, updateRequest);
        verify(jobRepository).save(job);
        verify(jobMapper).toResponse(job);
    }

    @Test
    void updateJob_shouldThrowException_whenJobDoesNotExist() {

        when(jobRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                JobNotFoundException.class,
                () -> jobService.updateJob(99L, updateRequest)
        );

        verify(jobRepository, never()).save(any());
    }

    @Test
    void deleteJob_shouldDeleteJob_whenJobExists() {

        when(jobRepository.existsById(1L))
                .thenReturn(true);

        jobService.deleteJob(1L);

        verify(jobRepository).existsById(1L);
        verify(jobRepository).deleteById(1L);
    }

    @Test
    void deleteJob_shouldThrowException_whenJobDoesNotExist() {

        when(jobRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                JobNotFoundException.class,
                () -> jobService.deleteJob(99L)
        );

        verify(jobRepository, never()).deleteById(any());
    }

    @Test
    void searchJobs_shouldSearchByLocation() {

        PageRequest pageable = PageRequest.of(0, 5);

        Page<Job> page =
                new PageImpl<>(List.of(job), pageable, 1);

        when(jobRepository.findByLocationContainingIgnoreCase(
                "Pune", pageable))
                .thenReturn(page);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        Page<JobResponse> result =
                jobService.searchJobs(
                        "Pune",
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(jobRepository)
                .findByLocationContainingIgnoreCase("Pune", pageable);
    }

    @Test
    void searchJobs_shouldSearchBySkills() {

        PageRequest pageable = PageRequest.of(0, 5);

        Page<Job> page =
                new PageImpl<>(List.of(job), pageable, 1);

        when(jobRepository.findBySkillsContainingIgnoreCase(
                "Java", pageable))
                .thenReturn(page);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        Page<JobResponse> result =
                jobService.searchJobs(
                        null,
                        "Java",
                        null,
                        null,
                        null,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(jobRepository)
                .findBySkillsContainingIgnoreCase("Java", pageable);
    }

    @Test
    void searchJobs_shouldFilterByMinimumSalary() {

        PageRequest pageable = PageRequest.of(0, 5);

        Page<Job> page =
                new PageImpl<>(List.of(job), pageable, 1);

        BigDecimal salary = new BigDecimal("70000");

        when(jobRepository.findBySalaryGreaterThanEqual(
                salary, pageable))
                .thenReturn(page);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        Page<JobResponse> result =
                jobService.searchJobs(
                        null,
                        null,
                        salary,
                        null,
                        null,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(jobRepository)
                .findBySalaryGreaterThanEqual(salary, pageable);
    }

    @Test
    void searchJobs_shouldFilterByJobType() {

        PageRequest pageable = PageRequest.of(0, 5);

        Page<Job> page =
                new PageImpl<>(List.of(job), pageable, 1);

        when(jobRepository.findByJobType(
                JobType.FULL_TIME, pageable))
                .thenReturn(page);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        Page<JobResponse> result =
                jobService.searchJobs(
                        null,
                        null,
                        null,
                        JobType.FULL_TIME,
                        null,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(jobRepository)
                .findByJobType(JobType.FULL_TIME, pageable);
    }

    @Test
    void searchJobs_shouldFilterByStatus() {

        PageRequest pageable = PageRequest.of(0, 5);

        Page<Job> page =
                new PageImpl<>(List.of(job), pageable, 1);

        when(jobRepository.findByStatus(
                JobStatus.DRAFT, pageable))
                .thenReturn(page);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        Page<JobResponse> result =
                jobService.searchJobs(
                        null,
                        null,
                        null,
                        null,
                        JobStatus.DRAFT,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(jobRepository)
                .findByStatus(JobStatus.DRAFT, pageable);
    }

    @Test
    void getJobsByEmployer_shouldReturnEmployerJobs() {

        PageRequest pageable = PageRequest.of(0, 5);

        Page<Job> page =
                new PageImpl<>(List.of(job), pageable, 1);

        when(jobRepository.findByEmployerId(
                100L, pageable))
                .thenReturn(page);

        when(jobMapper.toResponse(job))
                .thenReturn(jobResponse);

        Page<JobResponse> result =
                jobService.getJobsByEmployer(
                        100L,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(jobRepository)
                .findByEmployerId(100L, pageable);
    }
}