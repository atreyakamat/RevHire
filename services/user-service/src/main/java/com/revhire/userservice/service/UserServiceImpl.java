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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.JOB_SEEKER) {
            JobSeekerProfile profile = jobSeekerProfileRepository.findById(userId).orElse(null);
            return ProfileMapper.toResponse(user, profile);
        } else if (user.getRole() == Role.EMPLOYER) {
            EmployerProfile profile = employerProfileRepository.findById(userId).orElse(null);
            return ProfileMapper.toResponse(user, profile);
        }

        // Admin or standard user with no profile
        return ProfileMapper.toResponse(user, (JobSeekerProfile) null);
    }
}
