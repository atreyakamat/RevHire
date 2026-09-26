package com.revhire.notificationservice.dto.request;

import com.revhire.notificationservice.enums.NotificationChannel;
import com.revhire.notificationservice.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be 3-255 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(min = 5, max = 5000, message = "Message must be 5-5000 characters")
    private String message;
}