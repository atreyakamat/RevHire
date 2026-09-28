package com.revhire.applicationservice.service;

import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import com.revhire.applicationservice.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private com.revhire.applicationservice.client.NotificationClient notificationClient;

    @InjectMocks
    private ApplicationService applicationService;


    @Test
    void submitApplication_shouldSaveApplication() {

        Application application = new Application();
        application.setJobId(101L);
        application.setUserId(201L);
        application.setResumeId(301L);

        when(applicationRepository.save(application))
                .thenReturn(application);

        Application result =
                applicationService.submitApplication(application);

        assertNotNull(result);
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());
        assertNotNull(result.getAppliedAt());
        assertNotNull(result.getUpdatedAt());

        verify(applicationRepository).save(application);
    }


    @Test
    void getApplicationById_shouldReturnApplication() {

        Application application = new Application();
        application.setId(1L);

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        Application result =
                applicationService.getApplicationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(applicationRepository).findById(1L);
    }


    @Test
    void getApplicationById_shouldThrowExceptionWhenNotFound() {

        when(applicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationService.getApplicationById(999L)
        );

        assertEquals(
                "Application not found with id: 999",
                exception.getMessage()
        );

        verify(applicationRepository).findById(999L);
    }


    @Test
    void getAllApplications_shouldReturnApplications() {

        Application application1 = new Application();
        application1.setId(1L);

        Application application2 = new Application();
        application2.setId(2L);

        when(applicationRepository.findAll())
                .thenReturn(List.of(application1, application2));

        List<Application> result =
                applicationService.getAllApplications();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(applicationRepository).findAll();
    }


    @Test
    void getApplicationsByUser_shouldReturnApplications() {

        Application application = new Application();
        application.setUserId(201L);

        when(applicationRepository.findByUserId(201L))
                .thenReturn(List.of(application));

        List<Application> result =
                applicationService.getApplicationsByUser(201L);

        assertEquals(1, result.size());
        assertEquals(201L, result.get(0).getUserId());

        verify(applicationRepository).findByUserId(201L);
    }


    @Test
    void getApplicationsByJob_shouldReturnApplications() {

        Application application = new Application();
        application.setJobId(101L);

        when(applicationRepository.findByJobId(101L))
                .thenReturn(List.of(application));

        List<Application> result =
                applicationService.getApplicationsByJob(101L);

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getJobId());

        verify(applicationRepository).findByJobId(101L);
    }


    @Test
    void getApplicationsByStatus_shouldReturnApplications() {

        Application application = new Application();
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findByStatus(ApplicationStatus.APPLIED))
                .thenReturn(List.of(application));

        List<Application> result =
                applicationService.getApplicationsByStatus(
                        ApplicationStatus.APPLIED
                );

        assertEquals(1, result.size());
        assertEquals(
                ApplicationStatus.APPLIED,
                result.get(0).getStatus()
        );

        verify(applicationRepository)
                .findByStatus(ApplicationStatus.APPLIED);
    }


    @Test
    void updateApplicationStatus_shouldUpdateStatus() {

        Application application = new Application();
        application.setId(1L);
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(applicationRepository.save(application))
                .thenReturn(application);

        Application result =
                applicationService.updateApplicationStatus(
                        1L,
                        ApplicationStatus.SHORTLISTED
                );

        assertEquals(
                ApplicationStatus.SHORTLISTED,
                result.getStatus()
        );

        assertNotNull(result.getUpdatedAt());

        verify(applicationRepository).findById(1L);
        verify(applicationRepository).save(application);
    }


    @Test
    void deleteApplication_shouldDeleteApplication() {

        Application application = new Application();
        application.setId(1L);

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        applicationService.deleteApplication(1L);

        verify(applicationRepository).findById(1L);
        verify(applicationRepository).delete(application);
    }

    @Test
    void submitApplication_shouldDispatchNotification() {
        Application application = new Application();
        application.setJobId(101L);
        application.setUserId(201L);

        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.submitApplication(application);

        assertNotNull(result);
        verify(notificationClient).sendNotification(any(com.revhire.applicationservice.dto.request.NotificationRequest.class));
    }

    @Test
    void submitApplication_whenNotificationFails_shouldStillSucceed() {
        Application application = new Application();
        application.setJobId(101L);
        application.setUserId(201L);

        when(applicationRepository.save(application)).thenReturn(application);
        when(notificationClient.sendNotification(any())).thenThrow(new RuntimeException("Notification service unreachable"));

        Application result = applicationService.submitApplication(application);

        assertNotNull(result);
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());
        verify(applicationRepository).save(application);
    }

    @Test
    void updateApplicationStatus_shouldDispatchStatusNotification() {
        Application application = new Application();
        application.setId(1L);
        application.setJobId(101L);
        application.setUserId(201L);
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        applicationService.updateApplicationStatus(1L, ApplicationStatus.SHORTLISTED);

        verify(notificationClient).sendNotification(argThat(req ->
                "APPLICATION_SHORTLISTED".equals(req.getType()) &&
                Long.valueOf(201L).equals(req.getRecipientId())
        ));
    }

    @Test
    void updateApplicationStatus_whenNotificationFails_shouldStillUpdateStatus() {
        Application application = new Application();
        application.setId(1L);
        application.setJobId(101L);
        application.setUserId(201L);
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);
        when(notificationClient.sendNotification(any())).thenThrow(new RuntimeException("Connection timed out"));

        Application result = applicationService.updateApplicationStatus(1L, ApplicationStatus.HIRED);

        assertNotNull(result);
        assertEquals(ApplicationStatus.HIRED, result.getStatus());
        verify(applicationRepository).save(application);
    }
}