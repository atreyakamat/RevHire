package com.revhire.userservice.service;

import com.revhire.userservice.dto.response.UserProfileResponse;
import com.revhire.userservice.entity.EmployerProfile;
import com.revhire.userservice.entity.JobSeekerProfile;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import com.revhire.userservice.exception.ResourceNotFoundException;
import com.revhire.userservice.mapper.ProfileMapper;
import com.revhire.userservice.repository.EmployerProfileRepository;
import com.revhire.userservice.repository.JobSeekerProfileRepository;
import com.revhire.userservice.repository.UserRepository;
import com.revhire.userservice.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public UserServiceImpl(UserRepository userRepository, JobSeekerProfileRepository jobSeekerProfileRepository, EmployerProfileRepository employerProfileRepository) {
        this.userRepository = userRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails currentUser) {
            boolean isEmployer = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"));

            if (isEmployer && currentUser.getId() != null && !currentUser.getId().equals(user.getId()) && user.getRole() != Role.JOB_SEEKER) {
                throw new AccessDeniedException("Employers are only authorized to view Job Seeker profiles.");
            }
        }

        if (user.getRole() == Role.JOB_SEEKER) {
            JobSeekerProfile profile = jobSeekerProfileRepository.findById(userId).orElse(null);
            return ProfileMapper.toResponse(user, profile);
        } else if (user.getRole() == Role.EMPLOYER) {
            EmployerProfile profile = employerProfileRepository.findById(userId).orElse(null);
            return ProfileMapper.toResponse(user, profile);
        }

        return ProfileMapper.toResponse(user, (JobSeekerProfile) null);
    }
}
