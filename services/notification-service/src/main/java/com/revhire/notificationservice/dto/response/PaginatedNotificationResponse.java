package com.revhire.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedNotificationResponse {
    private List<NotificationResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}