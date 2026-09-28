package com.revhire.testservice.service;

import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.repository.TestRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TestRecordService {

    private final TestRecordRepository testRecordRepository;

    public TestRecordService(TestRecordRepository testRecordRepository) {
        this.testRecordRepository = testRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<TestRecord> getAllRecords() {
        return testRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TestRecord> getRecordById(Long id) {
        return testRecordRepository.findById(id);
    }

    public TestRecord createRecord(TestRecord testRecord) {
        testRecord.setId(null);
        if (testRecord.getCreatedAt() == null) {
            testRecord.setCreatedAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        }
        return testRecordRepository.save(testRecord);
    }

    public Optional<TestRecord> updateRecord(Long id, TestRecord testRecord) {
        return testRecordRepository.findById(id)
                .map(existing -> {
                    if (testRecord.getName() != null) {
                        existing.setName(testRecord.getName());
                    }
                    if (testRecord.getMessage() != null) {
                        existing.setMessage(testRecord.getMessage());
                    }
                    return testRecordRepository.save(existing);
                });
    }

    public boolean deleteRecord(Long id) {
        return testRecordRepository.findById(id)
                .map(existing -> {
                    testRecordRepository.delete(existing);
                    return true;
                })
                .orElse(false);
    }
}
