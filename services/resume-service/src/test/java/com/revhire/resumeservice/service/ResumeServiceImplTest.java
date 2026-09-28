package com.revhire.resumeservice.service;

import com.revhire.resumeservice.client.UserClient;
import com.revhire.resumeservice.dto.EducationDto;
import com.revhire.resumeservice.dto.ExperienceDto;
import com.revhire.resumeservice.dto.SkillDto;
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

import java.util.List;
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

        EducationDto edu = new EducationDto();
        edu.setDegree("B.S. CS");
        edu.setInstitution("MIT");
        edu.setStartDate("2016-09-01");
        edu.setEndDate("2020-05-30");
        request.setEducationList(List.of(edu));

        ExperienceDto exp = new ExperienceDto();
        exp.setJobTitle("Developer");
        exp.setCompany("Tech Co");
        exp.setStartDate("2020-06-01");
        exp.setEndDate("2023-01-01");
        exp.setDescription("Built microservices");
        request.setExperienceList(List.of(exp));

        SkillDto skill = new SkillDto();
        skill.setName("Java");
        skill.setProficiency("ADVANCED");
        request.setSkills(List.of(skill));

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
    void testGetResumeByJobSeekerId_Success_JobSeeker() {
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
    void testGetResumeByJobSeekerId_Success_Employer() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(50L);
        userDto.setRole("EMPLOYER");

        Resume resume = new Resume(10L, "Candidate Resume");
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

    @Test
    void testUpdateResume_Success() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        Resume existing = new Resume(10L, "Old summary");
        existing.setId(1L);

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(i -> i.getArgument(0));

        ResumeRequest request = new ResumeRequest();
        request.setSummary("New summary");

        EducationDto edu = new EducationDto();
        edu.setDegree("M.S.");
        request.setEducationList(List.of(edu));

        ExperienceDto exp = new ExperienceDto();
        exp.setJobTitle("Lead");
        request.setExperienceList(List.of(exp));

        SkillDto skill = new SkillDto();
        skill.setName("Spring Boot");
        request.setSkills(List.of(skill));

        ResumeResponse response = resumeService.updateResume(1L, request, token);

        assertNotNull(response);
        assertEquals("New summary", response.getSummary());
        assertEquals(1, response.getEducationList().size());
        assertEquals(1, response.getExperienceList().size());
        assertEquals(1, response.getSkills().size());
    }

    @Test
    void testUpdateResume_NotFound_ThrowsException() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        ResumeRequest request = new ResumeRequest();
        assertThrows(ResourceNotFoundException.class, () -> resumeService.updateResume(99L, request, token));
    }

    @Test
    void testUpdateResume_WrongOwner_ThrowsUnauthorized() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        Resume existing = new Resume(20L, "Other user's resume");
        existing.setId(1L);

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(existing));

        ResumeRequest request = new ResumeRequest();
        assertThrows(UnauthorizedAccessException.class, () -> resumeService.updateResume(1L, request, token));
    }

    @Test
    void testDeleteResume_Success() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        Resume existing = new Resume(10L, "Summary");
        existing.setId(1L);

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> resumeService.deleteResume(1L, token));
        verify(resumeRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteResume_NotFound_ThrowsException() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resumeService.deleteResume(99L, token));
    }

    @Test
    void testDeleteResume_WrongOwner_ThrowsUnauthorized() {
        String token = "Bearer valid.token";
        UserProfileDto userDto = new UserProfileDto();
        userDto.setId(10L);
        userDto.setRole("JOB_SEEKER");

        Resume existing = new Resume(20L, "Other resume");
        existing.setId(1L);

        when(userClient.getCurrentUser(token)).thenReturn(userDto);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedAccessException.class, () -> resumeService.deleteResume(1L, token));
        verify(resumeRepository, never()).delete(any());
    }
}
