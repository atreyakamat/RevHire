package com.revhire.resumeservice.dto.response;

import com.revhire.resumeservice.dto.EducationDto;
import com.revhire.resumeservice.dto.ExperienceDto;
import com.revhire.resumeservice.dto.SkillDto;
import java.util.List;

public class ResumeResponse {
    private Long id;
    private Long jobSeekerId;
    private String summary;
    private List<EducationDto> educationList;
    private List<ExperienceDto> experienceList;
    private List<SkillDto> skills;

    public ResumeResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(Long jobSeekerId) { this.jobSeekerId = jobSeekerId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<EducationDto> getEducationList() { return educationList; }
    public void setEducationList(List<EducationDto> educationList) { this.educationList = educationList; }
    public List<ExperienceDto> getExperienceList() { return experienceList; }
    public void setExperienceList(List<ExperienceDto> experienceList) { this.experienceList = experienceList; }
    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }
}
