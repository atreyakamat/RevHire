package com.revhire.applicationservice.client;

import com.revhire.applicationservice.dto.request.NotificationRequest;
import com.revhire.applicationservice.dto.response.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${notification-service.url:}",
        configuration = com.revhire.applicationservice.config.NotificationFeignConfig.class
)
public interface NotificationClient {

    @PostMapping("/api/notifications")
    NotificationResponse sendNotification(@RequestBody NotificationRequest request);
}
