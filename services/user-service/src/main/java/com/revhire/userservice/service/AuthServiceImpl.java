package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.LoginRequest;
import com.revhire.userservice.dto.request.UserRegistrationRequest;
import com.revhire.userservice.dto.response.AuthResponse;
import com.revhire.userservice.entity.EmployerProfile;
import com.revhire.userservice.entity.JobSeekerProfile;
import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import com.revhire.userservice.exception.UserAlreadyExistsException;
import com.revhire.userservice.mapper.UserMapper;
import com.revhire.userservice.repository.EmployerProfileRepository;
import com.revhire.userservice.repository.JobSeekerProfileRepository;
import com.revhire.userservice.repository.UserRepository;
import com.revhire.userservice.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                           JobSeekerProfileRepository jobSeekerProfileRepository,
                           EmployerProfileRepository employerProfileRepository,
                           PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse registerUser(UserRegistrationRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Registration with role ADMIN is not permitted");
        }
        if (request.getRole() == null) {
            request.setRole(Role.JOB_SEEKER);
        } else if (request.getRole() != Role.JOB_SEEKER && request.getRole() != Role.EMPLOYER) {
            throw new IllegalArgumentException("Registration role must be JOB_SEEKER or EMPLOYER");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered!");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.JOB_SEEKER) {
            JobSeekerProfile profile = new JobSeekerProfile(
                    savedUser,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhone(),
                    request.getDateOfBirth()
            );
            jobSeekerProfileRepository.save(profile);
        } else if (request.getRole() == Role.EMPLOYER) {
            EmployerProfile profile = new EmployerProfile(
                    savedUser,
                    request.getCompanyName(),
                    request.getContactName(),
                    request.getWebsite()
            );
            employerProfileRepository.save(profile);
        }

        return authenticateAndGenerateToken(request.getEmail(), request.getPassword(), savedUser);
    }

    @Override
    public AuthResponse authenticateUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return UserMapper.toAuthResponse(user, jwt);
    }

    private AuthResponse authenticateAndGenerateToken(String email, String password, User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return UserMapper.toAuthResponse(user, jwt);
    }
}
