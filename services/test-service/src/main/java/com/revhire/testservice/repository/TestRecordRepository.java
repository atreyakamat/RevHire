package com.revhire.testservice.repository;

import com.revhire.testservice.model.TestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRecordRepository extends JpaRepository<TestRecord, Long> {
}
