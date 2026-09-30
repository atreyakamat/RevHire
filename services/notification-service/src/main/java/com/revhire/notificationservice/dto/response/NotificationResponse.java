package com.revhire.notificationservice.dto.response;

import com.revhire.notificationservice.enums.NotificationChannel;
import com.revhire.notificationservice.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long recipientId;
    private NotificationType type;
    private NotificationChannel channel;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}