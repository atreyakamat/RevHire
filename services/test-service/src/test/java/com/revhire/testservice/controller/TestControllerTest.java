package com.revhire.testservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.repository.TestRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TestRecordRepository testRecordRepository;

    @InjectMocks
    private TestController testController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testPingEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("test-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void testDbEndpointSuccess() throws Exception {
        TestRecord record = new TestRecord("Integration test", LocalDateTime.now());
        record.setId(10L);
        when(testRecordRepository.save(any(TestRecord.class))).thenReturn(record);

        mockMvc.perform(get("/api/test/db"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("revhire_test"))
                .andExpect(jsonPath("$.recordId").value(10));
    }

    @Test
    void testDbEndpointFailure() throws Exception {
        when(testRecordRepository.save(any(TestRecord.class))).thenThrow(new RuntimeException("DB Connection failed"));

        mockMvc.perform(get("/api/test/db"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.error").value("DB Connection failed"));
    }

    @Test
    void testGetAllRecords() throws Exception {
        TestRecord r1 = new TestRecord("Record 1", LocalDateTime.now());
        r1.setId(1L);
        TestRecord r2 = new TestRecord("Record 2", LocalDateTime.now());
        r2.setId(2L);
        when(testRecordRepository.findAll()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/test/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].message").value("Record 1"))
                .andExpect(jsonPath("$[1].message").value("Record 2"));
    }

    @Test
    void testGetRecordByIdFound() throws Exception {
        TestRecord record = new TestRecord("Found record", LocalDateTime.now());
        record.setId(1L);
        when(testRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/test/records/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("Found record"));
    }

    @Test
    void testGetRecordByIdNotFound() throws Exception {
        when(testRecordRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/test/records/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateRecord() throws Exception {
        TestRecord saved = new TestRecord("Created record", LocalDateTime.now());
        saved.setId(1L);
        when(testRecordRepository.save(any(TestRecord.class))).thenReturn(saved);

        TestRecord input = new TestRecord("Created record", null);

        mockMvc.perform(post("/api/test/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("Created record"));
    }

    @Test
    void testUpdateRecordFound() throws Exception {
        TestRecord existing = new TestRecord("Old message", LocalDateTime.now());
        existing.setId(1L);
        TestRecord updated = new TestRecord("New message", existing.getCreatedAt());
        updated.setId(1L);

        when(testRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(testRecordRepository.save(any(TestRecord.class))).thenReturn(updated);

        TestRecord input = new TestRecord("New message", null);

        mockMvc.perform(put("/api/test/records/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("New message"));
    }

    @Test
    void testUpdateRecordNotFound() throws Exception {
        when(testRecordRepository.findById(999L)).thenReturn(Optional.empty());

        TestRecord input = new TestRecord("New message", null);

        mockMvc.perform(put("/api/test/records/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRecordFound() throws Exception {
        TestRecord existing = new TestRecord("To delete", LocalDateTime.now());
        existing.setId(1L);
        when(testRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(testRecordRepository).delete(existing);

        mockMvc.perform(delete("/api/test/records/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(testRecordRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteRecordNotFound() throws Exception {
        when(testRecordRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/test/records/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(testRecordRepository, never()).delete(any());
    }
}
