package com.revhire.notificationservice.service;

import com.revhire.notificationservice.dto.request.CreateNotificationRequest;
import com.revhire.notificationservice.dto.response.BulkActionResponse;
import com.revhire.notificationservice.dto.response.NotificationResponse;
import com.revhire.notificationservice.dto.response.PaginatedNotificationResponse;
import com.revhire.notificationservice.dto.response.UnreadCountResponse;
import com.revhire.notificationservice.entity.Notification;
import com.revhire.notificationservice.enums.NotificationChannel;
import com.revhire.notificationservice.enums.NotificationType;
import com.revhire.notificationservice.exception.NotificationNotFoundException;
import com.revhire.notificationservice.exception.UnauthorizedException;
import com.revhire.notificationservice.mapper.NotificationMapper;
import com.revhire.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    private NotificationMapper notificationMapper;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationMapper = new NotificationMapper();
        notificationService = new NotificationService(notificationRepository, notificationMapper, emailService);
    }

    @Test
    void testCreateNotification_InApp() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(5L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("App Submitted")
                .message("You submitted an application")
                .build();

        Notification saved = Notification.builder()
                .id(101L)
                .recipientId(5L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("App Submitted")
                .message("You submitted an application")
                .isRead(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(5L, response.getRecipientId());
        verify(emailService, never()).sendNotificationEmail(any());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotification_Email() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(5L)
                .type(NotificationType.APPLICATION_SHORTLISTED)
                .channel(NotificationChannel.EMAIL)
                .title("Shortlisted")
                .message("Shortlisted for job")
                .build();

        Notification saved = Notification.builder()
                .id(102L)
                .recipientId(5L)
                .type(NotificationType.APPLICATION_SHORTLISTED)
                .channel(NotificationChannel.EMAIL)
                .title("Shortlisted")
                .message("Shortlisted for job")
                .isRead(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(102L, response.getId());
        verify(emailService, times(1)).sendNotificationEmail(saved);
        // Saved once initially, once when updating sentAt
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testCreateNotification_Both() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(5L)
                .type(NotificationType.APPLICATION_SELECTED)
                .channel(NotificationChannel.BOTH)
                .title("Selected")
                .message("Offer selected")
                .build();

        Notification saved = Notification.builder()
                .id(103L)
                .recipientId(5L)
                .type(NotificationType.APPLICATION_SELECTED)
                .channel(NotificationChannel.BOTH)
                .title("Selected")
                .message("Offer selected")
                .isRead(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(103L, response.getId());
        verify(emailService, times(1)).sendNotificationEmail(saved);
    }

    @Test
    void testCreateNotification_EmailFails_Graceful() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(5L)
                .type(NotificationType.APPLICATION_REJECTED)
                .channel(NotificationChannel.EMAIL)
                .title("Status update")
                .message("Update message")
                .build();

        Notification saved = Notification.builder()
                .id(104L)
                .recipientId(5L)
                .type(NotificationType.APPLICATION_REJECTED)
                .channel(NotificationChannel.EMAIL)
                .title("Status update")
                .message("Update message")
                .isRead(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
        doThrow(new RuntimeException("Mail error")).when(emailService).sendNotificationEmail(any());

        assertDoesNotThrow(() -> {
            NotificationResponse response = notificationService.createNotification(request);
            assertNotNull(response);
            assertEquals(104L, response.getId());
        });
    }

    @Test
    void testGetNotificationById_Success() {
        Notification notification = Notification.builder()
                .id(1L)
                .recipientId(10L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("App Submitted")
                .message("Details")
                .isRead(false)
                .build();

        when(notificationRepository.notificationBelongsToUser(1L, 10L)).thenReturn(true);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.getNotificationById(1L, 10L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("App Submitted", response.getTitle());
    }

    @Test
    void testGetNotificationById_Unauthorized() {
        when(notificationRepository.notificationBelongsToUser(1L, 10L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> notificationService.getNotificationById(1L, 10L));
        verify(notificationRepository, never()).findById(any());
    }

    @Test
    void testGetNotificationById_NotFound() {
        when(notificationRepository.notificationBelongsToUser(1L, 10L)).thenReturn(true);
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.getNotificationById(1L, 10L));
    }

    @Test
    void testGetUserNotifications_PaginationHandling() {
        Notification n = Notification.builder()
                .id(1L)
                .recipientId(10L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("Note")
                .message("Msg")
                .isRead(false)
                .build();

        Page<Notification> page = new PageImpl<>(List.of(n));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(eq(10L), any(Pageable.class)))
                .thenReturn(page);

        // Test normal page
        PaginatedNotificationResponse response = notificationService.getUserNotifications(10L, 0, 10);
        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        // Test edge cases: page < 0, size <= 0, size > 100
        PaginatedNotificationResponse r2 = notificationService.getUserNotifications(10L, -1, 0);
        assertNotNull(r2);

        PaginatedNotificationResponse r3 = notificationService.getUserNotifications(10L, 0, 150);
        assertNotNull(r3);
    }

    @Test
    void testMarkAsRead_Success() {
        Notification notification = Notification.builder()
                .id(1L)
                .recipientId(10L)
                .isRead(false)
                .build();

        when(notificationRepository.findByIdAndRecipientId(1L, 10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L, 10L);

        assertNotNull(response);
        assertTrue(response.getIsRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void testMarkAsRead_UnauthorizedOrNotFound() {
        when(notificationRepository.findByIdAndRecipientId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> notificationService.markAsRead(1L, 10L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAllAsRead_Success() {
        when(notificationRepository.markAllAsReadByRecipientId(10L)).thenReturn(5);

        BulkActionResponse response = notificationService.markAllAsRead(10L);

        assertNotNull(response);
        assertEquals(5, response.getUpdatedCount());
        assertTrue(response.getMessage().contains("5 notifications marked as read"));
    }

    @Test
    void testDeleteNotification_Success() {
        Notification notification = Notification.builder()
                .id(1L)
                .recipientId(10L)
                .build();

        when(notificationRepository.findByIdAndRecipientId(1L, 10L)).thenReturn(Optional.of(notification));

        assertDoesNotThrow(() -> notificationService.deleteNotification(1L, 10L));
        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    void testDeleteNotification_UnauthorizedOrNotFound() {
        when(notificationRepository.findByIdAndRecipientId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> notificationService.deleteNotification(1L, 10L));
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void testGetUnreadCount_Success() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(10L)).thenReturn(7L);

        UnreadCountResponse response = notificationService.getUnreadCount(10L);

        assertNotNull(response);
        assertEquals(7L, response.getUnreadCount());
    }
}
