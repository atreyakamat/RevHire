package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.ProfileUpdateRequest;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_MSG = "User not found with id: ";

    private final UserRepository userRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public UserServiceImpl(UserRepository userRepository, JobSeekerProfileRepository jobSeekerProfileRepository, EmployerProfileRepository employerProfileRepository) {
        this.userRepository = userRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));

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

    @Override
    public UserProfileResponse updateUserProfile(Long userId, ProfileUpdateRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));

        // Profile update explicitly does NOT touch user.id, user.role, or user.password
        if (user.getRole() == Role.JOB_SEEKER) {
            return updateJobSeekerProfile(user, userId, request);
        } else if (user.getRole() == Role.EMPLOYER) {
            return updateEmployerProfile(user, userId, request);
        }

        return ProfileMapper.toResponse(user, (JobSeekerProfile) null);
    }

    private UserProfileResponse updateJobSeekerProfile(User user, Long userId, ProfileUpdateRequest request) {
        JobSeekerProfile profile = jobSeekerProfileRepository.findById(userId)
                .orElseGet(() -> new JobSeekerProfile(user, null, null, null, null));
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        jobSeekerProfileRepository.save(profile);
        return ProfileMapper.toResponse(user, profile);
    }

    private UserProfileResponse updateEmployerProfile(User user, Long userId, ProfileUpdateRequest request) {
        EmployerProfile profile = employerProfileRepository.findById(userId)
                .orElseGet(() -> new EmployerProfile(user, "Default Company", null, null));
        if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
        if (request.getContactName() != null) profile.setContactName(request.getContactName());
        if (request.getWebsite() != null) profile.setWebsite(request.getWebsite());
        employerProfileRepository.save(profile);
        return ProfileMapper.toResponse(user, profile);
    }

    @Override
    public UserProfileResponse updateUserRole(Long userId, Role newRole) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));

        user.setRole(newRole);
        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.JOB_SEEKER) {
            JobSeekerProfile profile = jobSeekerProfileRepository.findById(userId).orElse(null);
            return ProfileMapper.toResponse(savedUser, profile);
        } else if (savedUser.getRole() == Role.EMPLOYER) {
            EmployerProfile profile = employerProfileRepository.findById(userId).orElse(null);
            return ProfileMapper.toResponse(savedUser, profile);
        }

        return ProfileMapper.toResponse(savedUser, (JobSeekerProfile) null);
    }
}
