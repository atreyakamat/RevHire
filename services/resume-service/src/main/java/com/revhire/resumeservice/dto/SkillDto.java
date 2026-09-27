package com.revhire.resumeservice.dto;

public class SkillDto {
    private String name;
    private String proficiency;

    public SkillDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProficiency() { return proficiency; }
    public void setProficiency(String proficiency) { this.proficiency = proficiency; }
}
