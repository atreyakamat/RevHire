package com.revhire.applicationservice.mapper;

import com.revhire.applicationservice.dto.request.ApplicationRequest;
import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.entity.Application;

public class ApplicationMapper {

    public static Application toEntity(ApplicationRequest request) {

        Application application = new Application();

        application.setJobId(request.getJobId());
        application.setUserId(request.getUserId());
        application.setResumeId(request.getResumeId());

        return application;
    }

    public static ApplicationResponse toResponse(Application application) {

        ApplicationResponse response = new ApplicationResponse();

        response.setId(application.getId());
        response.setJobId(application.getJobId());
        response.setUserId(application.getUserId());
        response.setResumeId(application.getResumeId());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());

        return response;
    }
}