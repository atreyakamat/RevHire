package com.revhire.resumeservice.service;

import com.revhire.resumeservice.client.UserClient;
import com.revhire.resumeservice.dto.EducationDto;
import com.revhire.resumeservice.dto.ExperienceDto;
import com.revhire.resumeservice.dto.SkillDto;
import com.revhire.resumeservice.dto.UserProfileDto;
import com.revhire.resumeservice.dto.request.ResumeRequest;
import com.revhire.resumeservice.dto.response.ResumeResponse;
import com.revhire.resumeservice.entity.Education;
import com.revhire.resumeservice.entity.Experience;
import com.revhire.resumeservice.entity.Resume;
import com.revhire.resumeservice.entity.Skill;
import com.revhire.resumeservice.exception.ResourceNotFoundException;
import com.revhire.resumeservice.exception.UnauthorizedAccessException;
import com.revhire.resumeservice.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserClient userClient;

    @Override
    public ResumeResponse createResume(ResumeRequest request, String token) {
        UserProfileDto currentUser = userClient.getCurrentUser(token);

        if (!"JOB_SEEKER".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("Only Job Seekers can create a resume.");
        }

        if (resumeRepository.findByJobSeekerId(currentUser.getId()).isPresent()) {
            throw new IllegalArgumentException("Resume already exists for this Job Seeker.");
        }

        Resume resume = new Resume(currentUser.getId(), request.getSummary());
        mapRequestToEntity(request, resume);

        Resume savedResume = resumeRepository.save(resume);
        return mapEntityToResponse(savedResume);
    }

    @Override
    public ResumeResponse updateResume(Long resumeId, ResumeRequest request, String token) {
        UserProfileDto currentUser = userClient.getCurrentUser(token);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!"JOB_SEEKER".equals(currentUser.getRole()) || !resume.getJobSeekerId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only edit your own resume.");
        }

        resume.setSummary(request.getSummary());
        resume.getEducationList().clear();
        resume.getExperienceList().clear();
        resume.getSkills().clear();
        mapRequestToEntity(request, resume);

        Resume savedResume = resumeRepository.save(resume);
        return mapEntityToResponse(savedResume);
    }

    @Override
    public ResumeResponse getResumeByJobSeekerId(Long jobSeekerId, String token) {
        UserProfileDto currentUser = userClient.getCurrentUser(token);
        
        if ("JOB_SEEKER".equals(currentUser.getRole()) && !currentUser.getId().equals(jobSeekerId)) {
            throw new UnauthorizedAccessException("Job Seekers can only view their own resume.");
        }

        Resume resume = resumeRepository.findByJobSeekerId(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found for Job Seeker: " + jobSeekerId));

        return mapEntityToResponse(resume);
    }

    @Override
    public void deleteResume(Long resumeId, String token) {
        UserProfileDto currentUser = userClient.getCurrentUser(token);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!"JOB_SEEKER".equals(currentUser.getRole()) || !resume.getJobSeekerId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only delete your own resume.");
        }

        resumeRepository.delete(resume);
    }

    private void mapRequestToEntity(ResumeRequest request, Resume resume) {
        if (request.getEducationList() != null) {
            for (EducationDto dto : request.getEducationList()) {
                resume.addEducation(new Education(dto.getDegree(), dto.getInstitution(), dto.getStartDate(), dto.getEndDate()));
            }
        }
        if (request.getExperienceList() != null) {
            for (ExperienceDto dto : request.getExperienceList()) {
                resume.addExperience(new Experience(dto.getJobTitle(), dto.getCompany(), dto.getStartDate(), dto.getEndDate(), dto.getDescription()));
            }
        }
        if (request.getSkills() != null) {
            for (SkillDto dto : request.getSkills()) {
                resume.addSkill(new Skill(dto.getName(), dto.getProficiency()));
            }
        }
    }

    private ResumeResponse mapEntityToResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setJobSeekerId(resume.getJobSeekerId());
        response.setSummary(resume.getSummary());

        List<EducationDto> eduDtos = new ArrayList<>();
        if (resume.getEducationList() != null) {
            eduDtos = resume.getEducationList().stream().map(e -> {
                EducationDto d = new EducationDto();
                d.setDegree(e.getDegree());
                d.setInstitution(e.getInstitution());
                d.setStartDate(e.getStartDate());
                d.setEndDate(e.getEndDate());
                return d;
            }).collect(Collectors.toList());
        }
        response.setEducationList(eduDtos);

        List<ExperienceDto> expDtos = new ArrayList<>();
        if (resume.getExperienceList() != null) {
            expDtos = resume.getExperienceList().stream().map(e -> {
                ExperienceDto d = new ExperienceDto();
                d.setJobTitle(e.getJobTitle());
                d.setCompany(e.getCompany());
                d.setStartDate(e.getStartDate());
                d.setEndDate(e.getEndDate());
                d.setDescription(e.getDescription());
                return d;
            }).collect(Collectors.toList());
        }
        response.setExperienceList(expDtos);

        List<SkillDto> skillDtos = new ArrayList<>();
        if (resume.getSkills() != null) {
            skillDtos = resume.getSkills().stream().map(s -> {
                SkillDto d = new SkillDto();
                d.setName(s.getName());
                d.setProficiency(s.getProficiency());
                return d;
            }).collect(Collectors.toList());
        }
        response.setSkills(skillDtos);

        return response;
    }
}
