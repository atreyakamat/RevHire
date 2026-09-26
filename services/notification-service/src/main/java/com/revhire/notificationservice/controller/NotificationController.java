package com.revhire.notificationservice.controller;

import com.revhire.notificationservice.dto.request.CreateNotificationRequest;
import com.revhire.notificationservice.dto.response.BulkActionResponse;
import com.revhire.notificationservice.dto.response.NotificationResponse;
import com.revhire.notificationservice.dto.response.PaginatedNotificationResponse;
import com.revhire.notificationservice.dto.response.UnreadCountResponse;
import com.revhire.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * API 1: Create notification
     * POST /api/notifications
     * Called by other microservices (Application Service, Job Service, etc.)
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        log.info("POST /api/notifications - Creating notification");

        NotificationResponse response = notificationService.createNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API 2: Get specific notification by ID
     * GET /api/notifications/{id}
     * Requires authentication - user can only see their own notifications
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("GET /api/notifications/{} - Fetching notification", id);

        // TODO: Get actual userId from Security Context (for now, using header)
        if (userId == null) {
            userId = 1L; // Placeholder for testing
        }

        NotificationResponse response = notificationService.getNotificationById(id, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * API 3: Get all notifications for a user
     * GET /api/notifications/user/{userId}?page=0&size=20
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginatedNotificationResponse> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/notifications/user/{} - Fetching user notifications (page: {}, size: {})",
                userId, page, size);

        // TODO: Verify that requester is the actual user or admin

        PaginatedNotificationResponse response = notificationService.getUserNotifications(userId, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * API 4: Mark single notification as read
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("PUT /api/notifications/{}/read - Marking as read", id);

        if (userId == null) {
            userId = 1L; // Placeholder for testing
        }

        NotificationResponse response = notificationService.markAsRead(id, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * API 5: Mark all notifications as read (bulk operation)
     * PUT /api/notifications/user/{userId}/read
     */
    @PutMapping("/user/{userId}/read")
    public ResponseEntity<BulkActionResponse> markAllAsRead(
            @PathVariable Long userId) {

        log.info("PUT /api/notifications/user/{}/read - Marking all as read", userId);

        // TODO: Verify that requester is the actual user or admin

        BulkActionResponse response = notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(response);
    }

    /**
     * API 6: Delete notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("DELETE /api/notifications/{} - Deleting notification", id);

        if (userId == null) {
            userId = 1L; // Placeholder for testing
        }

        notificationService.deleteNotification(id, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * API 7: Get unread notification count
     * GET /api/notifications/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @PathVariable Long userId) {

        log.info("GET /api/notifications/user/{}/unread-count", userId);

        // TODO: Verify that requester is the actual user or admin

        UnreadCountResponse response = notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(response);
    }
}