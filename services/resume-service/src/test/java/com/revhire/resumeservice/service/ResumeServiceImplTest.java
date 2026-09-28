package com.revhire.resumeservice.service;

import com.revhire.resumeservice.client.UserClient;
import com.revhire.resumeservice.dto.UserProfileDto;
import com.revhire.resumeservice.dto.request.ResumeRequest;
import com.revhire.resumeservice.dto.response.ResumeResponse;
import com.revhire.resumeservice.entity.Resume;
import com.revhire.resumeservice.exception.ResourceNotFoundException;
import com.revhire.resumeservice.exception.UnauthorizedAccessException;
import com.revhire.resumeservice.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    @Test
    void testCreateResume_Success() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findByJobSeekerId(10L)).thenReturn(Optional.empty());

        Resume saved = new Resume(10L, "Software Engineer");
        saved.setId(100L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);

        ResumeRequest request = new ResumeRequest();
        request.setSummary("Software Engineer");

        ResumeResponse response = resumeService.createResume(request, token);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getJobSeekerId());
        assertEquals("Software Engineer", response.getSummary());
    }

    @Test
    void testCreateResume_NotJobSeeker_ThrowsUnauthorized() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(20L);
        userDto.setRole("EMPLOYER");

        when(userClient.getCurrentUser(token)).thenReturn(userDto);

        ResumeRequest request = new ResumeRequest();
        assertThrows(UnauthorizedAccessException.class, () -> resumeService.createResume(request, token));
        verify(resumeRepository, never()).save(any());
    }

    @Test
    void testCreateResume_AlreadyExists_ThrowsIllegalArgument() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findByJobSeekerId(10L)).thenReturn(Optional.of(new Resume(10L, "Existing")));

        ResumeRequest request = new ResumeRequest();
        assertThrows(IllegalArgumentException.class, () -> resumeService.createResume(request, token));
        verify(resumeRepository, never()).save(any());
    }

    @Test
    void testGetResumeByJobSeekerId_Success() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        Resume resume = new Resume(10L, "My Resume");
        resume.setId(1L);

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findByJobSeekerId(10L)).thenReturn(Optional.of(resume));

        ResumeResponse response = resumeService.getResumeByJobSeekerId(10L, token);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getJobSeekerId());
    }

    @Test
    void testGetResumeByJobSeekerId_ViewingOtherResume_ThrowsUnauthorized() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        when(userClient.getCurrentUser(token)).thenReturn(userDto);

        assertThrows(UnauthorizedAccessException.class, () -> resumeService.getResumeByJobSeekerId(99L, token));
    }

    @Test
    void testGetResumeByJobSeekerId_NotFound_ThrowsException() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findByJobSeekerId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resumeService.getResumeByJobSeekerId(10L, token));
    }
}
