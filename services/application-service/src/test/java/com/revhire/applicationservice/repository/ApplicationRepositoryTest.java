package com.revhire.applicationservice.repository;

import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase(replace = org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;


    @Test
    void findByUserId_shouldReturnApplications() {

        Application application = new Application();
        application.setJobId(101L);
        application.setUserId(201L);
        application.setResumeId(301L);
        application.setStatus(ApplicationStatus.APPLIED);

        applicationRepository.save(application);

        List<Application> result =
                applicationRepository.findByUserId(201L);

        assertEquals(1, result.size());
        assertEquals(201L, result.get(0).getUserId());
    }


    @Test
    void findByJobId_shouldReturnApplications() {

        Application application = new Application();
        application.setJobId(101L);
        application.setUserId(201L);
        application.setResumeId(301L);
        application.setStatus(ApplicationStatus.APPLIED);

        applicationRepository.save(application);

        List<Application> result =
                applicationRepository.findByJobId(101L);

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getJobId());
    }


    @Test
    void findByStatus_shouldReturnApplications() {

        Application application = new Application();
        application.setJobId(101L);
        application.setUserId(201L);
        application.setResumeId(301L);
        application.setStatus(ApplicationStatus.APPLIED);

        applicationRepository.save(application);

        List<Application> result =
                applicationRepository.findByStatus(
                        ApplicationStatus.APPLIED
                );

        assertEquals(1, result.size());
        assertEquals(
                ApplicationStatus.APPLIED,
                result.get(0).getStatus()
        );
    }


    @Test
    void findByUserId_shouldReturnEmptyListWhenNoApplicationsExist() {

        List<Application> result =
                applicationRepository.findByUserId(9999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}