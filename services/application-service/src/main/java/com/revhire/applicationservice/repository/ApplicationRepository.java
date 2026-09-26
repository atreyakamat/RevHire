package com.revhire.applicationservice.repository;

import com.revhire.applicationservice.entity.Application;
import com.revhire.applicationservice.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserId(Long userId);

    List<Application> findByJobId(Long jobId);

    List<Application> findByStatus(ApplicationStatus status);
}