package com.revhire.applicationservice.client;

import com.revhire.applicationservice.dto.response.JobResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobClient {

    @GetMapping("/api/jobs/{id}")
    JobResponse getJobById(@PathVariable("id") Long id);
}