package com.revhire.testservice.controller;

import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.repository.TestRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestRecordRepository testRecordRepository;

    public TestController(TestRecordRepository testRecordRepository) {
        this.testRecordRepository = testRecordRepository;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
            "service", "test-service",
            "status", "UP",
            "message", "test-service is reachable"
        );
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDb() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "test-service");
        response.put("database", "revhire_test");
        try {
            TestRecord record = new TestRecord("Integration test at " + LocalDateTime.now(), LocalDateTime.now());
            TestRecord saved = testRecordRepository.save(record);
            response.put("status", "UP");
            response.put("recordId", saved.getId());
            response.put("message", saved.getMessage());
            if (saved.getCreatedAt() != null) {
                response.put("createdAt", saved.getCreatedAt().toString());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
