package com.revhire.testservice.controller;

import com.revhire.testservice.dto.TestRecordRequest;
import com.revhire.testservice.dto.TestRecordResponse;
import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.service.TestRecordService;
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

import java.util.List;

@RestController
@RequestMapping("/api/test/records")
public class TestRecordController {

    private final TestRecordService testRecordService;

    public TestRecordController(TestRecordService testRecordService) {
        this.testRecordService = testRecordService;
    }

    @GetMapping
    public List<TestRecordResponse> getAllRecords() {
        return testRecordService.getAllRecords().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestRecordResponse> getRecordById(@PathVariable("id") Long id) {
        return testRecordService.getRecordById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TestRecordResponse> createRecord(@RequestBody TestRecordRequest request) {
        TestRecord testRecord = new TestRecord(request.getName(), request.getMessage());
        TestRecord created = testRecordService.createRecord(testRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestRecordResponse> updateRecord(@PathVariable("id") Long id, @RequestBody TestRecordRequest request) {
        TestRecord testRecord = new TestRecord(request.getName(), request.getMessage());
        return testRecordService.updateRecord(id, testRecord)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("id") Long id) {
        if (testRecordService.deleteRecord(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private TestRecordResponse toResponse(TestRecord testRecord) {
        return new TestRecordResponse(
                testRecord.getId(),
                testRecord.getName(),
                testRecord.getMessage(),
                testRecord.getCreatedAt()
        );
    }
}
