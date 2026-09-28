package com.revhire.resumeservice.dto;

import com.revhire.resumeservice.dto.request.ResumeRequest;
import com.revhire.resumeservice.dto.response.ResumeResponse;
import com.revhire.resumeservice.entity.Education;
import com.revhire.resumeservice.entity.Experience;
import com.revhire.resumeservice.entity.Resume;
import com.revhire.resumeservice.entity.Skill;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testEducationDto() {
        EducationDto dto = new EducationDto();
        dto.setDegree("B.S.");
        dto.setInstitution("Univ");
        dto.setStartDate("2020-01-01");
        dto.setEndDate("2024-01-01");

        assertEquals("B.S.", dto.getDegree());
        assertEquals("Univ", dto.getInstitution());
        assertEquals("2020-01-01", dto.getStartDate());
        assertEquals("2024-01-01", dto.getEndDate());
    }

    @Test
    void testExperienceDto() {
        ExperienceDto dto = new ExperienceDto();
        dto.setJobTitle("Engineer");
        dto.setCompany("Corp");
        dto.setStartDate("2020");
        dto.setEndDate("2022");
        dto.setDescription("Dev");

        assertEquals("Engineer", dto.getJobTitle());
        assertEquals("Corp", dto.getCompany());
        assertEquals("2020", dto.getStartDate());
        assertEquals("2022", dto.getEndDate());
        assertEquals("Dev", dto.getDescription());
    }

    @Test
    void testSkillDto() {
        SkillDto dto = new SkillDto();
        dto.setName("Java");
        dto.setProficiency("EXPERT");

        assertEquals("Java", dto.getName());
        assertEquals("EXPERT", dto.getProficiency());
    }

    @Test
    void testUserProfileDto() {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(5L);
        dto.setEmail("u@r.com");
        dto.setRole("JOB_SEEKER");

        assertEquals(5L, dto.getId());
        assertEquals("u@r.com", dto.getEmail());
        assertEquals("JOB_SEEKER", dto.getRole());
    }

    @Test
    void testResumeEntityAndDtos() {
        Resume resume = new Resume();
        resume.setId(1L);
        resume.setJobSeekerId(10L);
        resume.setSummary("Summary");
        resume.setEducationList(new ArrayList<>());
        resume.setExperienceList(new ArrayList<>());
        resume.setSkills(new ArrayList<>());

        Education edu = new Education();
        edu.setId(1L);
        edu.setDegree("BS");
        edu.setInstitution("MIT");
        edu.setStartDate("2018");
        edu.setEndDate("2022");
        edu.setResume(resume);

        Experience exp = new Experience();
        exp.setId(1L);
        exp.setJobTitle("SWE");
        exp.setCompany("Google");
        exp.setStartDate("2022");
        exp.setEndDate("2024");
        exp.setDescription("Work");
        exp.setResume(resume);

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Go");
        skill.setProficiency("ADVANCED");
        skill.setResume(resume);

        resume.addEducation(edu);
        resume.addExperience(exp);
        resume.addSkill(skill);

        assertEquals(1L, edu.getId());
        assertEquals("BS", edu.getDegree());
        assertEquals("MIT", edu.getInstitution());
        assertEquals("2018", edu.getStartDate());
        assertEquals("2022", edu.getEndDate());
        assertEquals(resume, edu.getResume());

        assertEquals(1L, exp.getId());
        assertEquals("SWE", exp.getJobTitle());
        assertEquals("Google", exp.getCompany());
        assertEquals("2022", exp.getStartDate());
        assertEquals("2024", exp.getEndDate());
        assertEquals("Work", exp.getDescription());
        assertEquals(resume, exp.getResume());

        assertEquals(1L, skill.getId());
        assertEquals("Go", skill.getName());
        assertEquals("ADVANCED", skill.getProficiency());
        assertEquals(resume, skill.getResume());
    }
}
