package com.revhire.testservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.service.TestRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TestRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TestRecordService testRecordService;

    @InjectMocks
    private TestRecordController testRecordController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testRecordController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllRecords() throws Exception {
        TestRecord r1 = new TestRecord("Test 1", "Hello RevHire");
        r1.setId(1L);
        TestRecord r2 = new TestRecord("Test 2", "Another message");
        r2.setId(2L);
        when(testRecordService.getAllRecords()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/test/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test 1"))
                .andExpect(jsonPath("$[0].message").value("Hello RevHire"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Test 2"))
                .andExpect(jsonPath("$[1].message").value("Another message"));
    }

    @Test
    void testGetRecordByIdFound() throws Exception {
        TestRecord record = new TestRecord("Test 1", "Hello RevHire");
        record.setId(1L);
        when(testRecordService.getRecordById(1L)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/test/records/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test 1"))
                .andExpect(jsonPath("$.message").value("Hello RevHire"));
    }

    @Test
    void testGetRecordByIdNotFound() throws Exception {
        when(testRecordService.getRecordById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/test/records/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateRecord() throws Exception {
        TestRecord saved = new TestRecord("Test 1", "Hello RevHire");
        saved.setId(1L);
        when(testRecordService.createRecord(any(TestRecord.class))).thenReturn(saved);

        TestRecord input = new TestRecord("Test 1", "Hello RevHire");

        mockMvc.perform(post("/api/test/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test 1"))
                .andExpect(jsonPath("$.message").value("Hello RevHire"));
    }

    @Test
    void testUpdateRecordFound() throws Exception {
        TestRecord updated = new TestRecord("Test 1 Updated", "Updated message");
        updated.setId(1L);
        when(testRecordService.updateRecord(eq(1L), any(TestRecord.class))).thenReturn(Optional.of(updated));

        TestRecord input = new TestRecord("Test 1 Updated", "Updated message");

        mockMvc.perform(put("/api/test/records/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test 1 Updated"))
                .andExpect(jsonPath("$.message").value("Updated message"));
    }

    @Test
    void testUpdateRecordNotFound() throws Exception {
        when(testRecordService.updateRecord(eq(999L), any(TestRecord.class))).thenReturn(Optional.empty());

        TestRecord input = new TestRecord("Test 1 Updated", "Updated message");

        mockMvc.perform(put("/api/test/records/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRecordFound() throws Exception {
        when(testRecordService.deleteRecord(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/test/records/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(testRecordService, times(1)).deleteRecord(1L);
    }

    @Test
    void testDeleteRecordNotFound() throws Exception {
        when(testRecordService.deleteRecord(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/test/records/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(testRecordService, times(1)).deleteRecord(999L);
    }
}
