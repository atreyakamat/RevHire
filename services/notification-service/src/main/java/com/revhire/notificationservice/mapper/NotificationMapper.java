package com.revhire.notificationservice.mapper;

import com.revhire.notificationservice.dto.request.CreateNotificationRequest;
import com.revhire.notificationservice.dto.response.NotificationResponse;
import com.revhire.notificationservice.entity.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public Notification toEntity(CreateNotificationRequest request) {
        return Notification.builder()
                .recipientId(request.getRecipientId())
                .type(request.getType())
                .channel(request.getChannel())
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Convert Notification Entity to NotificationResponse
     */
    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }

    /**
     * Convert List of Notification Entities to List of NotificationResponse
     */
    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}