package com.revhire.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revhire.notificationservice.dto.request.CreateNotificationRequest;
import com.revhire.notificationservice.dto.response.BulkActionResponse;
import com.revhire.notificationservice.dto.response.NotificationResponse;
import com.revhire.notificationservice.dto.response.PaginatedNotificationResponse;
import com.revhire.notificationservice.dto.response.UnreadCountResponse;
import com.revhire.notificationservice.enums.NotificationChannel;
import com.revhire.notificationservice.enums.NotificationType;
import com.revhire.notificationservice.exception.GlobalExceptionHandler;
import com.revhire.notificationservice.exception.NotificationNotFoundException;
import com.revhire.notificationservice.exception.UnauthorizedException;
import com.revhire.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateNotification_Success() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(10L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("App Submitted")
                .message("Your application was received.")
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .recipientId(10L)
                .title("App Submitted")
                .build();

        when(notificationService.createNotification(any(CreateNotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.recipientId").value(10L));
    }

    @Test
    void testCreateNotification_RecipientSpoofing_Forbidden() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(20L) // Recipient is user 20
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("Spoofed Notification")
                .message("Attacker sending notification to victim")
                .build();

        // Caller is user 10 (not recipient, not admin, not internal service)
        mockMvc.perform(post("/api/notifications")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testCreateNotification_AsAdmin_Success() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(20L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("Admin Broadcast")
                .message("System notification")
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(2L)
                .recipientId(20L)
                .title("Admin Broadcast")
                .build();

        when(notificationService.createNotification(any(CreateNotificationRequest.class))).thenReturn(response);

        org.springframework.security.core.Authentication adminAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        1L, null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(post("/api/notifications")
                        .principal(adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.recipientId").value(20L));
    }

    @Test
    void testCreateNotification_AsInternalService_Success() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(20L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .channel(NotificationChannel.IN_APP)
                .title("Service Dispatch")
                .message("Application service dispatched notification")
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(3L)
                .recipientId(20L)
                .title("Service Dispatch")
                .build();

        when(notificationService.createNotification(any(CreateNotificationRequest.class))).thenReturn(response);

        org.springframework.security.core.Authentication serviceAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "internal-service", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE")));

        mockMvc.perform(post("/api/notifications")
                        .principal(serviceAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.recipientId").value(20L));
    }

    @Test
    void testCreateNotification_ValidationError() throws Exception {
        CreateNotificationRequest invalidRequest = CreateNotificationRequest.builder()
                .title("") // Blank title
                .build();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testGetNotification_Success() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .recipientId(10L)
                .title("Test Notification")
                .build();

        when(notificationService.getNotificationById(eq(1L), eq(10L), eq(false))).thenReturn(response);

        mockMvc.perform(get("/api/notifications/1")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Notification"));
    }

    @Test
    void testGetNotification_Unauthenticated_Rejected() throws Exception {
        // Without authentication or X-User-Id header, access is rejected (no fallback to userId=1L)
        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testGetNotification_NotFound() throws Exception {
        when(notificationService.getNotificationById(eq(999L), eq(10L), eq(false)))
                .thenThrow(NotificationNotFoundException.withId(999L));

        mockMvc.perform(get("/api/notifications/999")
                        .header("X-User-Id", 10L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testGetNotification_Unauthorized() throws Exception {
        when(notificationService.getNotificationById(eq(1L), eq(2L), eq(false)))
                .thenThrow(UnauthorizedException.userNotAllowed(2L, 1L));

        mockMvc.perform(get("/api/notifications/1")
                        .header("X-User-Id", 2L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testGetUserNotifications_Success() throws Exception {
        PaginatedNotificationResponse pageResponse = PaginatedNotificationResponse.builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(notificationService.getUserNotifications(10L, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/notifications/user/10")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testGetUserNotifications_CrossUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/notifications/user/20")
                        .header("X-User-Id", 10L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .isRead(true)
                .build();

        when(notificationService.markAsRead(eq(1L), eq(10L), eq(false))).thenReturn(response);

        mockMvc.perform(put("/api/notifications/1/read")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    void testMarkAllAsRead_Success() throws Exception {
        BulkActionResponse response = BulkActionResponse.builder()
                .updatedCount(3)
                .message("3 notifications marked as read")
                .build();

        when(notificationService.markAllAsRead(10L)).thenReturn(response);

        mockMvc.perform(put("/api/notifications/user/10/read")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(3));
    }

    @Test
    void testMarkAllAsRead_CrossUser_Forbidden() throws Exception {
        mockMvc.perform(put("/api/notifications/user/20/read")
                        .header("X-User-Id", 10L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testDeleteNotification_Success() throws Exception {
        doNothing().when(notificationService).deleteNotification(eq(1L), eq(10L), eq(false));

        mockMvc.perform(delete("/api/notifications/1")
                        .header("X-User-Id", 10L))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetUnreadCount_Success() throws Exception {
        UnreadCountResponse response = UnreadCountResponse.builder()
                .unreadCount(5L)
                .build();

        when(notificationService.getUnreadCount(10L)).thenReturn(response);

        mockMvc.perform(get("/api/notifications/user/10/unread-count")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5L));
    }

    @Test
    void testGetUnreadCount_CrossUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/notifications/user/20/unread-count")
                        .header("X-User-Id", 10L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testInternalServerError_Handled() throws Exception {
        when(notificationService.getUnreadCount(10L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/notifications/user/10/unread-count")
                        .header("X-User-Id", 10L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
