package com.medev.hrstream.jobapplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard summary statistics for all applications of a job.
 * Used by HR to get a quick overview of candidate evaluation metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobApplicationDashboardDTO {

    // Job identification
    private String jobId;

    // Application counts by status
    private Integer totalApplications;
    private Integer submittedCount;
    private Integer underReviewCount;
    private Integer shortlistedCount;
    private Integer rejectedCount;
    private Integer hiredCount;

    // Scoring statistics
    private Double averageRuleScore;    // 0-100
    private Double averageAiScore;      // 0-100
    private Double averageOverallScore; // 0-100

    // Processing status
    private Integer processingCount;    // Still being evaluated
    private Integer failedCount;        // Failed to evaluate

    // Utility method to get status distribution as percentage
    public Integer getSubmittedPercentage() {
        if (totalApplications == 0) return 0;
        return Math.round((float) submittedCount / totalApplications * 100);
    }

    public Integer getShortlistedPercentage() {
        if (totalApplications == 0) return 0;
        return Math.round((float) shortlistedCount / totalApplications * 100);
    }

    public Integer getRejectedPercentage() {
        if (totalApplications == 0) return 0;
        return Math.round((float) rejectedCount / totalApplications * 100);
    }

    public Integer getHiredPercentage() {
        if (totalApplications == 0) return 0;
        return Math.round((float) hiredCount / totalApplications * 100);
    }

    public Integer getProcessingPercentage() {
        if (totalApplications == 0) return 0;
        return Math.round((float) processingCount / totalApplications * 100);
    }
}

