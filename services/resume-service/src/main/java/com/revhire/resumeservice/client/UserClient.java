package com.revhire.resumeservice.client;

import com.revhire.resumeservice.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/me")
    UserProfileDto getCurrentUser(@RequestHeader("Authorization") String token);
}
