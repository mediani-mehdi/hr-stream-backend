package com.medev.hrstream.jobapplication;

import com.medev.hrstream.jobapplication.dto.ApplicationDetailDTO;
import com.medev.hrstream.jobapplication.dto.ApplicationDtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for HR panel operations on job applications.
 * Provides enhanced views with scoring and evaluation data.
 */
@Slf4j
@Service
public class HrPanelApplicationService {

    private final JobApplicationRepository repository;
    private final ApplicationDtoMapper dtoMapper;

    public HrPanelApplicationService(JobApplicationRepository repository, ApplicationDtoMapper dtoMapper) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
    }

    /**
     * Get detailed application information with scoring data for HR panel.
     * Includes all evaluation metrics and AI reasoning.
     */
    public ApplicationDetailDTO getApplicationDetail(String applicationId) {
        JobApplication app = repository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        return dtoMapper.toDetailDTO(app);
    }

    /**
     * Get all applications for a job with scoring summaries, paginated.
     * Used by HR to review all candidates for a job.
     */
    public Page<ApplicationDetailDTO> getApplicationsForJob(String jobId, Pageable pageable) {
        Page<JobApplication> appPage = repository.findByJobId(jobId, pageable);
        List<ApplicationDetailDTO> dtos = appPage.getContent().stream()
                .map(dtoMapper::toDetailDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, appPage.getTotalElements());
    }

    /**
     * Get applications sorted by overall score (highest first).
     * Used for ranking candidates by their evaluation results.
     */
    public List<ApplicationDetailDTO> getApplicationsForJobSortedByScore(String jobId) {
        List<JobApplication> apps = repository.findByJobId(jobId);
        return apps.stream()
                .map(dtoMapper::toDetailDTO)
                .sorted((a, b) -> {
                    Integer scoreA = a.getScoringSummary() != null ? a.getScoringSummary().getOverallScore() : 0;
                    Integer scoreB = b.getScoringSummary() != null ? b.getScoringSummary().getOverallScore() : 0;
                    return scoreB.compareTo(scoreA); // Descending order
                })
                .collect(Collectors.toList());
    }

    /**
     * Get applications with a specific status (SHORTLISTED, REJECTED, etc.)
     * with full scoring details.
     */
    public List<ApplicationDetailDTO> getApplicationsByStatus(ApplicationStatus status) {
        return repository.findByStatus(status).stream()
                .map(dtoMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get applications for a specific candidate with full scoring details.
     */
    public List<ApplicationDetailDTO> getApplicationsForCandidate(String candidateId) {
        return repository.findByCandidateId(candidateId).stream()
                .map(dtoMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard summary statistics for a job.
     */
    public JobApplicationDashboardDTO getJobDashboard(String jobId) {
        List<JobApplication> applications = repository.findByJobId(jobId);

        if (applications.isEmpty()) {
            return JobApplicationDashboardDTO.builder()
                    .jobId(jobId)
                    .totalApplications(0)
                    .submittedCount(0)
                    .underReviewCount(0)
                    .shortlistedCount(0)
                    .rejectedCount(0)
                    .hiredCount(0)
                    .averageRuleScore(0.0)
                    .averageAiScore(0.0)
                    .averageOverallScore(0.0)
                    .processingCount(0)
                    .failedCount(0)
                    .build();
        }

        long submitted = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED).count();
        long underReview = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_REVIEW).count();
        long shortlisted = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
        long rejected = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();
        long hired = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.HIRED).count();

        double avgRuleScore = applications.stream()
                .filter(a -> a.getRuleScore() != null)
                .mapToInt(JobApplication::getRuleScore)
                .average()
                .orElse(0.0);

        double avgAiScore = applications.stream()
                .filter(a -> a.getAiScore() != null)
                .mapToInt(JobApplication::getAiScore)
                .average()
                .orElse(0.0);

        double avgOverallScore = (avgRuleScore * 0.4) + (avgAiScore * 0.6);

        long processing = applications.stream()
                .filter(a -> a.getPipelineStatus() != null &&
                        (a.getPipelineStatus().name().contains("QUEUED") ||
                         a.getPipelineStatus().name().contains("EXTRACTING") ||
                         a.getPipelineStatus().name().contains("SCORING")))
                .count();

        long failed = applications.stream()
                .filter(a -> a.getPipelineStatus() != null && a.getPipelineStatus().name().contains("FAILED"))
                .count();

        return JobApplicationDashboardDTO.builder()
                .jobId(jobId)
                .totalApplications(applications.size())
                .submittedCount((int) submitted)
                .underReviewCount((int) underReview)
                .shortlistedCount((int) shortlisted)
                .rejectedCount((int) rejected)
                .hiredCount((int) hired)
                .averageRuleScore(Math.round(avgRuleScore * 100.0) / 100.0)
                .averageAiScore(Math.round(avgAiScore * 100.0) / 100.0)
                .averageOverallScore(Math.round(avgOverallScore * 100.0) / 100.0)
                .processingCount((int) processing)
                .failedCount((int) failed)
                .build();
    }
}

