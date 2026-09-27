package com.revhire.testservice.controller;

import com.revhire.testservice.model.TestRecord;
import com.revhire.testservice.repository.TestRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TestRecordRepository testRecordRepository;

    @InjectMocks
    private TestController testController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController).build();
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
}
