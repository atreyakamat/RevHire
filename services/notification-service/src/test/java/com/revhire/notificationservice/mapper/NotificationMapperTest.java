package com.revhire.notificationservice.mapper;

import com.revhire.notificationservice.dto.request.CreateNotificationRequest;
import com.revhire.notificationservice.dto.response.NotificationResponse;
import com.revhire.notificationservice.entity.Notification;
import com.revhire.notificationservice.enums.NotificationChannel;
import com.revhire.notificationservice.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }

    @Test
    void testToEntity() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(10L)
                .type(NotificationType.APPLICATION_SHORTLISTED)
                .channel(NotificationChannel.EMAIL)
                .title("Shortlisted!")
                .message("Your application was shortlisted.")
                .build();

        Notification entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(10L, entity.getRecipientId());
        assertEquals(NotificationType.APPLICATION_SHORTLISTED, entity.getType());
        assertEquals(NotificationChannel.EMAIL, entity.getChannel());
        assertEquals("Shortlisted!", entity.getTitle());
        assertEquals("Your application was shortlisted.", entity.getMessage());
        assertFalse(entity.getIsRead());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void testToResponse() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Notification entity = Notification.builder()
                .id(1L)
                .recipientId(10L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("Submitted")
                .message("App submitted successfully")
                .isRead(true)
                .createdAt(now)
                .sentAt(now)
                .build();

        NotificationResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getRecipientId());
        assertEquals(NotificationType.APPLICATION_SUBMITTED, response.getType());
        assertEquals(NotificationChannel.IN_APP, response.getChannel());
        assertEquals("Submitted", response.getTitle());
        assertEquals("App submitted successfully", response.getMessage());
        assertTrue(response.getIsRead());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getSentAt());
    }

    @Test
    void testToResponseList() {
        Notification n1 = Notification.builder()
                .id(1L)
                .recipientId(10L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("N1")
                .message("M1")
                .isRead(false)
                .build();
        Notification n2 = Notification.builder()
                .id(2L)
                .recipientId(10L)
                .type(NotificationType.APPLICATION_UNDER_REVIEW)
                .channel(NotificationChannel.BOTH)
                .title("N2")
                .message("M2")
                .isRead(true)
                .build();

        List<NotificationResponse> responses = mapper.toResponseList(List.of(n1, n2));

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
    }
}
