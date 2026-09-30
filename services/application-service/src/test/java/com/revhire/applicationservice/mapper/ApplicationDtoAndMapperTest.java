package com.revhire.applicationservice.mapper;

import com.revhire.applicationservice.ApplicationServiceApplication;
import com.revhire.applicationservice.dto.request.ApplicationRequest;
import com.revhire.applicationservice.dto.request.NotificationRequest;
import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.dto.response.JobResponse;
import com.revhire.applicationservice.dto.response.NotificationResponse;
import com.revhire.applicationservice.dto.response.UserProfileResponse;
import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDtoAndMapperTest {

    @Test
    void testApplicationMapper() {
        ApplicationRequest req = new ApplicationRequest();
        req.setJobId(1L);
        req.setUserId(2L);
        req.setResumeId(3L);

        Application entity = ApplicationMapper.toEntity(req);
        assertNotNull(entity);
        assertEquals(1L, entity.getJobId());
        assertEquals(2L, entity.getUserId());
        assertEquals(3L, entity.getResumeId());

        LocalDateTime now = LocalDateTime.now();
        entity.setId(10L);
        entity.setStatus(ApplicationStatus.APPLIED);
        entity.setAppliedAt(now);
        entity.setUpdatedAt(now);

        ApplicationResponse res = ApplicationMapper.toResponse(entity);
        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals(1L, res.getJobId());
        assertEquals(2L, res.getUserId());
        assertEquals(3L, res.getResumeId());
        assertEquals(ApplicationStatus.APPLIED, res.getStatus());
        assertEquals(now, res.getAppliedAt());
        assertEquals(now, res.getUpdatedAt());
    }

    @Test
    void testDtos() {
        NotificationRequest notif = new NotificationRequest(10L, "APP_SUBMITTED", "IN_APP", "Title", "Msg");
        notif.setRecipientId(20L);
        notif.setType("STATUS_UPDATE");
        notif.setChannel("EMAIL");
        notif.setTitle("T");
        notif.setMessage("M");

        assertEquals(20L, notif.getRecipientId());
        assertEquals("STATUS_UPDATE", notif.getType());
        assertEquals("EMAIL", notif.getChannel());
        assertEquals("T", notif.getTitle());
        assertEquals("M", notif.getMessage());

        JobResponse job = new JobResponse();
        job.setId(5L);
        job.setTitle("Dev");
        job.setDescription("Backend Dev");
        job.setLocation("Remote");
        assertEquals(5L, job.getId());
        assertEquals("Dev", job.getTitle());
        assertEquals("Backend Dev", job.getDescription());
        assertEquals("Remote", job.getLocation());

        UserProfileResponse user = new UserProfileResponse();
        user.setId(7L);
        user.setEmail("a@b.com");
        user.setRole("JOB_SEEKER");
        assertEquals(7L, user.getId());
        assertEquals("a@b.com", user.getEmail());
        assertEquals("JOB_SEEKER", user.getRole());

        NotificationResponse notifRes = new NotificationResponse();
        notifRes.setId(8L);
        notifRes.setRecipientId(7L);
        notifRes.setTitle("Hello");
        assertEquals(8L, notifRes.getId());
        assertEquals(7L, notifRes.getRecipientId());
        assertEquals("Hello", notifRes.getTitle());

        Application appWithConstructor = new Application(1L, 2L, 3L, ApplicationStatus.SHORTLISTED, LocalDateTime.now(), LocalDateTime.now());
        assertNotNull(appWithConstructor);
    }

    @Test
    void testApplicationServiceApplication() {
        ApplicationServiceApplication app = new ApplicationServiceApplication();
        assertNotNull(app);
    }
}
