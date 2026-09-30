package com.revhire.testservice.service;

import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.repository.TestRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestRecordServiceTest {

    @Mock
    private TestRecordRepository testRecordRepository;

    @InjectMocks
    private TestRecordService testRecordService;

    @Test
    void testGetAllRecords() {
        TestRecord record = new TestRecord("Test 1", "Message 1");
        when(testRecordRepository.findAll()).thenReturn(List.of(record));

        List<TestRecord> records = testRecordService.getAllRecords();
        assertEquals(1, records.size());
        assertEquals("Test 1", records.get(0).getName());
    }

    @Test
    void testGetRecordById() {
        TestRecord record = new TestRecord("Test 1", "Message 1");
        record.setId(10L);
        when(testRecordRepository.findById(10L)).thenReturn(Optional.of(record));

        Optional<TestRecord> result = testRecordService.getRecordById(10L);
        assertTrue(result.isPresent());
        assertEquals("Test 1", result.get().getName());
    }

    @Test
    void testCreateRecord() {
        TestRecord record = new TestRecord("Test 1", "Message 1");
        when(testRecordRepository.save(any(TestRecord.class))).thenAnswer(i -> {
            TestRecord saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        TestRecord created = testRecordService.createRecord(record);
        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        assertEquals("Test 1", created.getName());
    }

    @Test
    void testUpdateRecord_Found() {
        TestRecord existing = new TestRecord("Old Name", "Old Message");
        existing.setId(5L);
        when(testRecordRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(testRecordRepository.save(any(TestRecord.class))).thenAnswer(i -> i.getArgument(0));

        TestRecord updateInfo = new TestRecord("New Name", "New Message");
        Optional<TestRecord> updated = testRecordService.updateRecord(5L, updateInfo);

        assertTrue(updated.isPresent());
        assertEquals("New Name", updated.get().getName());
        assertEquals("New Message", updated.get().getMessage());
    }

    @Test
    void testUpdateRecord_NotFound() {
        when(testRecordRepository.findById(99L)).thenReturn(Optional.empty());

        TestRecord updateInfo = new TestRecord("New Name", "New Message");
        Optional<TestRecord> updated = testRecordService.updateRecord(99L, updateInfo);

        assertFalse(updated.isPresent());
    }

    @Test
    void testDeleteRecord_Found() {
        TestRecord existing = new TestRecord("To Delete", "Msg");
        existing.setId(7L);
        when(testRecordRepository.findById(7L)).thenReturn(Optional.of(existing));

        boolean deleted = testRecordService.deleteRecord(7L);
        assertTrue(deleted);
        verify(testRecordRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteRecord_NotFound() {
        when(testRecordRepository.findById(99L)).thenReturn(Optional.empty());

        boolean deleted = testRecordService.deleteRecord(99L);
        assertFalse(deleted);
        verify(testRecordRepository, never()).delete(any());
    }
}
