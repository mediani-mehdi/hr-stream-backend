package com.medev.hrstream.dashboard;

import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.JobApplication;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HrDashboardService {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public HrDashboardService(JobRepository jobRepository,
                              CandidateRepository candidateRepository,
                              JobApplicationRepository jobApplicationRepository) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public HrDashboardStatsResponse getStats() {
        long totalJobs = jobRepository.countByDeletedFalse();
        long totalCandidates = candidateRepository.count();
        long totalApplications = jobApplicationRepository.count();
        
        long openJobsCount = jobRepository.countByDeletedFalseAndStatus(JobStatus.OPEN);
        long closedJobsCount = jobRepository.countByDeletedFalseAndStatus(JobStatus.FILLED) +
                jobRepository.countByDeletedFalseAndStatus(JobStatus.CANCELLED);
        
        long hiredCount = jobApplicationRepository.countByStatus(ApplicationStatus.HIRED);
        long rejectedCount = jobApplicationRepository.countByStatus(ApplicationStatus.REJECTED);
        
        List<JobApplication> latestApplications = jobApplicationRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "applicationDate"))
        ).getContent();

        return HrDashboardStatsResponse.builder()
                .totalJobs(totalJobs)
                .totalCandidates(totalCandidates)
                .totalApplications(totalApplications)
                .openJobsCount(openJobsCount)
                .closedJobsCount(closedJobsCount)
                .hiredCount(hiredCount)
                .rejectedCount(rejectedCount)
                .latestApplications(latestApplications)
                .build();
    }
}
