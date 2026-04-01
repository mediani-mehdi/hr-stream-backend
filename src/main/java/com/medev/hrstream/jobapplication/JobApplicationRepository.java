package com.medev.hrstream.jobapplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, String> {
    boolean existsByJobIdAndCandidateId(String jobId, String candidateId);
}
