package com.revhire.testservice.controller;

import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.repository.TestRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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
            response.put("createdAt", saved.getCreatedAt().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    @GetMapping("/records")
    public List<TestRecord> getAllRecords() {
        return testRecordRepository.findAll();
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<TestRecord> getRecordById(@PathVariable("id") Long id) {
        return testRecordRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/records")
    public ResponseEntity<TestRecord> createRecord(@RequestBody TestRecord record) {
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        TestRecord saved = testRecordRepository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<TestRecord> updateRecord(@PathVariable("id") Long id, @RequestBody TestRecord record) {
        return testRecordRepository.findById(id)
                .map(existing -> {
                    if (record.getMessage() != null) {
                        existing.setMessage(record.getMessage());
                    }
                    if (record.getCreatedAt() != null) {
                        existing.setCreatedAt(record.getCreatedAt());
                    }
                    TestRecord updated = testRecordRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("id") Long id) {
        return testRecordRepository.findById(id)
                .map(existing -> {
                    testRecordRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
