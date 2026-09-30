package com.revhire.resumeservice.dto;

public class EducationDto {
    private String degree;
    private String institution;
    private String startDate;
    private String endDate;

    public EducationDto() {
        // Default constructor required by JPA and Jackson
    }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
