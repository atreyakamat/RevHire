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

    public TestRecord createRecord(TestRecord record) {
        record.setId(null);
        return testRecordRepository.save(record);
    }

    public Optional<TestRecord> updateRecord(Long id, TestRecord record) {
        return testRecordRepository.findById(id)
                .map(existing -> {
                    if (record.getName() != null) {
                        existing.setName(record.getName());
                    }
                    if (record.getMessage() != null) {
                        existing.setMessage(record.getMessage());
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
