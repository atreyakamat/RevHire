package com.revhire.testservice.dto;

import com.revhire.testservice.model.TestRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TestRecordDtoTest {

    @Test
    void testTestRecordEntity() {
        LocalDateTime now = LocalDateTime.now();
        TestRecord entityRecord = new TestRecord();
        entityRecord.setId(1L);
        entityRecord.setName("Entity Name");
        entityRecord.setMessage("Entity Message");
        entityRecord.setCreatedAt(now);

        assertEquals(1L, entityRecord.getId());
        assertEquals("Entity Name", entityRecord.getName());
        assertEquals("Entity Message", entityRecord.getMessage());
        assertEquals(now, entityRecord.getCreatedAt());

        TestRecord r2 = new TestRecord("Name 2", "Msg 2");
        assertEquals("Name 2", r2.getName());

        TestRecord r3 = new TestRecord("Msg 3", now);
        assertEquals("Test Record", r3.getName());
        assertEquals("Msg 3", r3.getMessage());

        TestRecord r4 = new TestRecord("Name 4", "Msg 4", now);
        assertEquals("Name 4", r4.getName());

        TestRecord r5 = new TestRecord();
        r5.prePersist();
        assertNotNull(r5.getCreatedAt());
    }

    @Test
    void testTestRecordResponse() {
        LocalDateTime now = LocalDateTime.now();
        TestRecordResponse resp = new TestRecordResponse();
        resp.setId(10L);
        resp.setName("Resp Name");
        resp.setMessage("Resp Msg");
        resp.setCreatedAt(now);

        assertEquals(10L, resp.getId());
        assertEquals("Resp Name", resp.getName());
        assertEquals("Resp Msg", resp.getMessage());
        assertEquals(now, resp.getCreatedAt());

        TestRecordResponse resp2 = new TestRecordResponse(20L, "N2", "M2", now);
        assertEquals(20L, resp2.getId());
    }

    @Test
    void testTestRecordRequest() {
        TestRecordRequest req = new TestRecordRequest();
        req.setName("Req Name");
        req.setMessage("Req Msg");

        assertEquals("Req Name", req.getName());
        assertEquals("Req Msg", req.getMessage());

        TestRecordRequest req2 = new TestRecordRequest("Req Name 2", "Req Msg 2");
        assertEquals("Req Name 2", req2.getName());
    }
}
