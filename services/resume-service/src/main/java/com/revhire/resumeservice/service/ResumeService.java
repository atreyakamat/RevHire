package com.revhire.resumeservice.service;

import com.revhire.resumeservice.dto.request.ResumeRequest;
import com.revhire.resumeservice.dto.response.ResumeResponse;

public interface ResumeService {
    ResumeResponse createResume(ResumeRequest request, String token);
    ResumeResponse updateResume(Long resumeId, ResumeRequest request, String token);
    ResumeResponse getResumeByJobSeekerId(Long jobSeekerId, String token);
    void deleteResume(Long resumeId, String token);
}
