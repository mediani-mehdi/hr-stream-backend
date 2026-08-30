package com.medev.hrstream.jobapplication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medev.hrstream.jobapplication.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Complete application details for the HR panel dashboard.
 * Includes candidate info, job info, and comprehensive scoring data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationDetailDTO {

    // Application identification
    private String applicationId;
    private LocalDateTime applicationDate;
    private ApplicationStatus status;

    // Candidate information
    private String candidateId;
    private String candidateName;
    private String candidateEmail;

    // Job information
    private String jobId;
    private String jobTitle;
    private String jobDescription;

    // Scoring data
    private ScoringResultDTO scoring;

    // Quick access summary
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScoringQuickSummary {
        private Integer overallScore;      // 0-100
        private Integer ruleScore;          // 0-100
        private Integer aiScore;            // 0-100
        private String scoringStatus;       // DONE, PROCESSING, FAILED
        private String aiProvider;          // e.g., "Gemini", "OpenAI"
    }

    private ScoringQuickSummary scoringSummary;
}

