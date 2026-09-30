package com.revhire.resumeservice.controller;

import com.revhire.resumeservice.dto.request.ResumeRequest;
import com.revhire.resumeservice.dto.response.ResumeResponse;
import com.revhire.resumeservice.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeControllerTest {

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private ResumeController resumeController;

    @Test
    void testCreateResume_Success() {
        ResumeRequest request = new ResumeRequest();
        request.setSummary("Experienced Software Engineer");
        String token = "Bearer test.jwt.token";

        ResumeResponse mockResponse = new ResumeResponse();
        mockResponse.setId(1L);
        mockResponse.setJobSeekerId(10L);
        mockResponse.setSummary("Experienced Software Engineer");

        when(resumeService.createResume(request, token)).thenReturn(mockResponse);

        ResponseEntity<ResumeResponse> response = resumeController.createResume(request, token);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Experienced Software Engineer", response.getBody().getSummary());
        verify(resumeService, times(1)).createResume(request, token);
    }

    @Test
    void testGetResumeByJobSeekerId_Success() {
        String token = "Bearer test.jwt.token";
        ResumeResponse mockResponse = new ResumeResponse();
        mockResponse.setId(2L);
        mockResponse.setJobSeekerId(10L);
        mockResponse.setSummary("Backend Developer");

        when(resumeService.getResumeByJobSeekerId(10L, token)).thenReturn(mockResponse);

        ResponseEntity<ResumeResponse> response = resumeController.getResumeByJobSeekerId(10L, token);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, response.getBody().getId());
        verify(resumeService, times(1)).getResumeByJobSeekerId(10L, token);
    }

    @Test
    void testUpdateResume_Success() {
        ResumeRequest request = new ResumeRequest();
        request.setSummary("Updated Summary");
        String token = "Bearer test.jwt.token";

        ResumeResponse mockResponse = new ResumeResponse();
        mockResponse.setId(1L);
        mockResponse.setSummary("Updated Summary");

        when(resumeService.updateResume(1L, request, token)).thenReturn(mockResponse);

        ResponseEntity<ResumeResponse> response = resumeController.updateResume(1L, request, token);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Summary", response.getBody().getSummary());
        verify(resumeService, times(1)).updateResume(1L, request, token);
    }

    @Test
    void testDeleteResume_Success() {
        String token = "Bearer test.jwt.token";
        doNothing().when(resumeService).deleteResume(1L, token);

        ResponseEntity<Void> response = resumeController.deleteResume(1L, token);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(resumeService, times(1)).deleteResume(1L, token);
    }
}
