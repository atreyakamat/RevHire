package com.revhire.resumeservice.repository;

import com.revhire.resumeservice.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByJobSeekerId(Long jobSeekerId);
}
