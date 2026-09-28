package com.revhire.applicationservice.service;

import com.revhire.applicationservice.client.NotificationClient;
import com.revhire.applicationservice.dto.request.NotificationRequest;
import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final NotificationClient notificationClient;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this(applicationRepository, null);
    }

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository,
                              @Autowired(required = false) NotificationClient notificationClient) {
        this.applicationRepository = applicationRepository;
        this.notificationClient = notificationClient;
    }

    // Submit a new application
    public Application submitApplication(Application application) {

        application.setStatus(ApplicationStatus.APPLIED);

        LocalDateTime now = LocalDateTime.now();
        application.setAppliedAt(now);
        application.setUpdatedAt(now);

        Application saved = applicationRepository.save(application);

        sendNotificationSafely(
                saved.getUserId(),
                "APPLICATION_SUBMITTED",
                "Application Submitted",
                "Your application for job #" + saved.getJobId() + " has been successfully submitted."
        );

        return saved;
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

        Application updated = applicationRepository.save(application);

        triggerStatusNotification(updated, status);

        return updated;
    }

    private void triggerStatusNotification(Application application, ApplicationStatus status) {
        if (application == null || application.getUserId() == null || status == null) {
            return;
        }

        String type;
        String title;
        String message;

        switch (status) {
            case UNDER_REVIEW:
                type = "APPLICATION_UNDER_REVIEW";
                title = "Application Under Review";
                message = "Your application for job #" + application.getJobId() + " is now under review.";
                break;
            case SHORTLISTED:
                type = "APPLICATION_SHORTLISTED";
                title = "Application Shortlisted";
                message = "Congratulations! Your application for job #" + application.getJobId() + " has been shortlisted.";
                break;
            case REJECTED:
                type = "APPLICATION_REJECTED";
                title = "Application Status Update";
                message = "Thank you for your interest. Your application for job #" + application.getJobId() + " was not selected.";
                break;
            case HIRED:
                type = "APPLICATION_SELECTED";
                title = "Application Selected";
                message = "Congratulations! You have been selected for job #" + application.getJobId() + ".";
                break;
            default:
                return;
        }

        sendNotificationSafely(application.getUserId(), type, title, message);
    }

    private void sendNotificationSafely(Long recipientId, String type, String title, String message) {
        if (notificationClient == null || recipientId == null) {
            return;
        }
        try {
            NotificationRequest request = new NotificationRequest(
                    recipientId,
                    type,
                    "IN_APP",
                    title,
                    message
            );
            notificationClient.sendNotification(request);
            log.info("Sent {} notification to user {}", type, recipientId);
        } catch (Exception e) {
            log.warn("Failed to dispatch notification to user {}: {}", recipientId, e.getMessage());
        }
    }

    // Delete application
    public void deleteApplication(Long id) {

        Application application = getApplicationById(id);

        applicationRepository.delete(application);
    }
}