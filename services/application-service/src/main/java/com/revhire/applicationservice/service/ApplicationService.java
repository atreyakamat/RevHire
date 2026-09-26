package com.revhire.applicationservice.service;

import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    // Submit a new application
    public Application submitApplication(Application application) {

        application.setStatus(ApplicationStatus.APPLIED);

        LocalDateTime now = LocalDateTime.now();
        application.setAppliedAt(now);
        application.setUpdatedAt(now);

        return applicationRepository.save(application);
    }

    // Get application by ID
    public Application getApplicationById(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Application not found with id: " + id));
    }

    // Get all applications
    public List<Application> getAllApplications() {

        return applicationRepository.findAll();
    }

    // Get applications submitted by a job seeker
    public List<Application> getApplicationsByUser(Long userId) {

        return applicationRepository.findByUserId(userId);
    }

    // Get applications for a job
    public List<Application> getApplicationsByJob(Long jobId) {

        return applicationRepository.findByJobId(jobId);
    }

    // Get applications by status
    public List<Application> getApplicationsByStatus(ApplicationStatus status) {

        return applicationRepository.findByStatus(status);
    }

    // Update application status
    public Application updateApplicationStatus(
            Long id,
            ApplicationStatus status) {

        Application application = getApplicationById(id);

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    // Delete application
    public void deleteApplication(Long id) {

        Application application = getApplicationById(id);

        applicationRepository.delete(application);
    }
}