package com.revhire.notificationservice.service;

import com.revhire.notificationservice.dto.request.CreateNotificationRequest;
import com.revhire.notificationservice.dto.response.BulkActionResponse;
import com.revhire.notificationservice.dto.response.NotificationResponse;
import com.revhire.notificationservice.dto.response.PaginatedNotificationResponse;
import com.revhire.notificationservice.dto.response.UnreadCountResponse;
import com.revhire.notificationservice.entity.Notification;
import com.revhire.notificationservice.enums.NotificationChannel;
import com.revhire.notificationservice.exception.NotificationNotFoundException;
import com.revhire.notificationservice.exception.UnauthorizedException;
import com.revhire.notificationservice.mapper.NotificationMapper;
import com.revhire.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.emailService = emailService;
    }

    /**
     * Create a new notification
     * Called by other microservices (Application Service, Job Service, etc.)
     */
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for recipient: {}, type: {}",
                request.getRecipientId(), request.getType());

        // Convert request to entity
        Notification notification = notificationMapper.toEntity(request);

        // Save to database
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created with ID: {}", savedNotification.getId());

        // Send based on channel
        sendNotificationByChannel(savedNotification);

        return notificationMapper.toResponse(savedNotification);
    }

    /**
     * Get notification by ID (with authorization check)
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id, Long userId) {
        log.info("Fetching notification ID: {} for user: {}", id, userId);

        // Verify user owns this notification
        boolean ownsNotification = notificationRepository.notificationBelongsToUser(id, userId);
        if (!ownsNotification) {
            throw UnauthorizedException.userNotAllowed(userId, id);
        }

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> NotificationNotFoundException.withId(id));

        return notificationMapper.toResponse(notification);
    }

    /**
     * Get all notifications for a user (paginated)
     */
    @Transactional(readOnly = true)
    public PaginatedNotificationResponse getUserNotifications(Long userId, int page, int size) {
        log.info("Fetching notifications for user: {} (page: {}, size: {})", userId, page, size);

        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponse> responses = notificationMapper
                .toResponseList(notificationPage.getContent());

        return PaginatedNotificationResponse.builder()
                .content(responses)
                .pageNumber(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .build();
    }

    /**
     * Mark a specific notification as read
     */
    public NotificationResponse markAsRead(Long id, Long userId) {
        log.info("Marking notification ID: {} as read for user: {}", id, userId);

        // Verify user owns this notification
        Notification notification = notificationRepository.findByIdAndRecipientId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Notification not found or user not authorized: id={}, userId={}", id, userId);
                    return UnauthorizedException.userNotAllowed(userId, id);
                });

        // Mark as read
        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        log.info("Notification ID: {} marked as read", id);
        return notificationMapper.toResponse(updatedNotification);
    }

    /**
     * Mark all notifications as read for a user (bulk operation)
     */
    public BulkActionResponse markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        int updatedCount = notificationRepository.markAllAsReadByRecipientId(userId);

        log.info("Marked {} notifications as read for user: {}", updatedCount, userId);

        return BulkActionResponse.builder()
                .updatedCount(updatedCount)
                .message(updatedCount + " notifications marked as read")
                .build();
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long id, Long userId) {
        log.info("Deleting notification ID: {} for user: {}", id, userId);

        // Verify user owns this notification
        Notification notification = notificationRepository.findByIdAndRecipientId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Notification not found or user not authorized for deletion: id={}, userId={}", id, userId);
                    return UnauthorizedException.userNotAllowed(userId, id);
                });

        notificationRepository.delete(notification);
        log.info("Notification ID: {} deleted", id);
    }

    /**
     * Get unread notification count for a user
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        log.info("Fetching unread count for user: {}", userId);

        long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(userId);

        return UnreadCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * Send notification based on channel configuration
     */
    private void sendNotificationByChannel(Notification notification) {
        try {
            if (notification.getChannel() == NotificationChannel.IN_APP) {
                sendInAppNotification(notification);
            } else if (notification.getChannel() == NotificationChannel.EMAIL) {
                sendEmailNotification(notification);
            } else if (notification.getChannel() == NotificationChannel.BOTH) {
                sendInAppNotification(notification);
                sendEmailNotification(notification);
            }
        } catch (Exception e) {
            log.error("Error sending notification ID: {}", notification.getId(), e);
            // Don't throw - notification should remain in DB even if delivery fails
            // Delivery can be retried later
        }
    }

    /**
     * Send In-App Notification (just mark as persistent in DB)
     */
    private void sendInAppNotification(Notification notification) {
        log.info("In-app notification prepared for user: {} (ID: {})",
                notification.getRecipientId(), notification.getId());
        // In-app notifications are stored in DB and retrieved via API
        // No additional action needed here
    }

    /**
     * Send Email Notification
     */
    private void sendEmailNotification(Notification notification) {
        log.info("Sending email notification to user: {} (ID: {})",
                notification.getRecipientId(), notification.getId());

        try {
            emailService.sendNotificationEmail(notification);

            // Update sentAt timestamp
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Email sent successfully for notification ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email for notification ID: {}", notification.getId(), e);
            // Don't throw - continue with other channels
        }
    }
}