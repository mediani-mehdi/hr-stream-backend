package com.medev.hrstream.jobapplication;

import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, String> {

    boolean existsByJobIdAndCandidateId(String jobId, String candidateId);

    long countByJobId(String jobId);

    long countByJobIdAndStatusNot(String jobId, ApplicationStatus status);

    List<JobApplication> findByPipelineStatus(PipelineStatus pipelineStatus);

    List<JobApplication> findByCandidateId(String candidateId);

    List<JobApplication> findByJobId(String jobId);

    @Query("SELECT a FROM JobApplication a WHERE a.pipelineStatus IN :statuses " +
           "AND (a.pipelineLastAttemptAt IS NULL OR a.pipelineLastAttemptAt < :threshold)")
    List<JobApplication> findStuckApplications(
            @Param("statuses") List<PipelineStatus> statuses,
            @Param("threshold") LocalDateTime threshold);

    long countByStatus(ApplicationStatus status);

    // HR Panel methods
    Page<JobApplication> findByJobId(String jobId, Pageable pageable);

    List<JobApplication> findByStatus(ApplicationStatus status);
}
