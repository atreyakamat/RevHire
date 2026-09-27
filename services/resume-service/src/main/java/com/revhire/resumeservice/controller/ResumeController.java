package com.revhire.resumeservice.controller;

import com.revhire.resumeservice.dto.request.ResumeRequest;
import com.revhire.resumeservice.dto.response.ResumeResponse;
import com.revhire.resumeservice.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(@RequestBody ResumeRequest request,
                                                       @RequestHeader("Authorization") String token) {
        ResumeResponse response = resumeService.createResume(request, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> updateResume(@PathVariable("resumeId") Long resumeId,
                                                       @RequestBody ResumeRequest request,
                                                       @RequestHeader("Authorization") String token) {
        ResumeResponse response = resumeService.updateResume(resumeId, request, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{jobSeekerId}")
    public ResponseEntity<ResumeResponse> getResumeByJobSeekerId(@PathVariable("jobSeekerId") Long jobSeekerId,
                                                                 @RequestHeader("Authorization") String token) {
        ResumeResponse response = resumeService.getResumeByJobSeekerId(jobSeekerId, token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(@PathVariable("resumeId") Long resumeId,
                                             @RequestHeader("Authorization") String token) {
        resumeService.deleteResume(resumeId, token);
        return ResponseEntity.noContent().build();
    }
}
