package com.revhire.applicationservice.client;

import com.revhire.applicationservice.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user-service.url:}")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserProfileResponse getUserById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable("id") Long id
    );

    @GetMapping("/api/users/me")
    UserProfileResponse getCurrentUser(@RequestHeader("Authorization") String token);
}
