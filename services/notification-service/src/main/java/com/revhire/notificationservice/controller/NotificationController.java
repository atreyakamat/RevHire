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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private Long getAuthenticatedUserId(Authentication authentication, Long headerUserId) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long l) {
                return l;
            }
            if (principal != null) {
                try {
                    return Long.parseLong(principal.toString());
                } catch (NumberFormatException ignored) {
                    // Ignore and fallback to header if needed
                }
            }
        }
        if (headerUserId != null) {
            return headerUserId;
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private boolean isCallerAdminOrInternal(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_INTERNAL_SERVICE".equals(a.getAuthority()));
    }

    /**
     * API 1: Create notification
     * POST /api/notifications
     * Restricted to internal services, ADMIN, or self-notifications by authenticated users.
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {

        log.info("POST /api/notifications - Creating notification");

        boolean authorizedCaller = isCallerAdminOrInternal(authentication);
        if (!authorizedCaller) {
            Long authUserId = getAuthenticatedUserId(authentication, headerUserId);
            if (!authUserId.equals(request.getRecipientId())) {
                throw new AccessDeniedException("Unauthorized: users cannot create notifications for other recipients");
            }
        }

        NotificationResponse response = notificationService.createNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API 2: Get specific notification by ID
     * GET /api/notifications/{id}
     * Requires authentication - user can only see their own notifications (or admin)
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {

        log.info("GET /api/notifications/{} - Fetching notification", id);

        Long authUserId = getAuthenticatedUserId(authentication, headerUserId);
        boolean admin = isAdmin(authentication);

        NotificationResponse response = notificationService.getNotificationById(id, authUserId, admin);

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
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {

        log.info("GET /api/notifications/user/{} - Fetching user notifications (page: {}, size: {})",
                userId, page, size);

        Long authUserId = getAuthenticatedUserId(authentication, headerUserId);
        if (!isAdmin(authentication) && !authUserId.equals(userId)) {
            throw new AccessDeniedException("Access denied: cannot view notifications of another user");
        }

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
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {

        log.info("PUT /api/notifications/{}/read - Marking as read", id);

        Long authUserId = getAuthenticatedUserId(authentication, headerUserId);
        boolean admin = isAdmin(authentication);

        NotificationResponse response = notificationService.markAsRead(id, authUserId, admin);

        return ResponseEntity.ok(response);
    }

    /**
     * API 5: Mark all notifications as read (bulk operation)
     * PUT /api/notifications/user/{userId}/read
     */
    @PutMapping("/user/{userId}/read")
    public ResponseEntity<BulkActionResponse> markAllAsRead(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {

        log.info("PUT /api/notifications/user/{}/read - Marking all as read", userId);

        Long authUserId = getAuthenticatedUserId(authentication, headerUserId);
        if (!isAdmin(authentication) && !authUserId.equals(userId)) {
            throw new AccessDeniedException("Access denied: cannot mark notifications as read for another user");
        }

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
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {

        log.info("DELETE /api/notifications/{} - Deleting notification", id);

        Long authUserId = getAuthenticatedUserId(authentication, headerUserId);
        boolean admin = isAdmin(authentication);

        notificationService.deleteNotification(id, authUserId, admin);

        return ResponseEntity.noContent().build();
    }

    /**
     * API 7: Get unread notification count
     * GET /api/notifications/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {

        log.info("GET /api/notifications/user/{}/unread-count", userId);

        Long authUserId = getAuthenticatedUserId(authentication, headerUserId);
        if (!isAdmin(authentication) && !authUserId.equals(userId)) {
            throw new AccessDeniedException("Access denied: cannot view unread notification count for another user");
        }

        UnreadCountResponse response = notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(response);
    }
}