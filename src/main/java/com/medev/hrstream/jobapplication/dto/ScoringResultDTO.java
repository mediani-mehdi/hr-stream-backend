package com.medev.hrstream.jobapplication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Complete scoring results for an application.
 * Used by the HR panel dashboard to display candidate evaluation data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScoringResultDTO {

    // Pipeline processing status
    private PipelineStatus pipelineStatus;
    private LocalDateTime processedAt;
    private Integer pipelineAttemptCount;

    // CV extraction info
    private Integer cvExtractedChars;

    // Rule-based scoring (keyword/skill matching)
    private Integer ruleScore;
    private ScoringDetailDTO ruleScoreDetails;

    // AI-based deep scoring
    private Integer aiScore;
    private String aiReasoning;
    private String aiProvider;

    // Overall composite score (0-100)
    private Integer overallScore;

    // Error information (if pipeline failed)
    private ProcessingErrorCode processingErrorCode;
    private String processingErrorMessage;

    /**
     * Calculates an overall score from rule and AI scores.
     * Weighting: 40% rule score + 60% AI score (if both available)
     * If only one is available, uses that.
     */
    public Integer calculateOverallScore() {
        if (ruleScore == null && aiScore == null) {
            return null;
        }
        if (ruleScore == null) {
            return aiScore;
        }
        if (aiScore == null) {
            return ruleScore;
        }
        // Weighted average: 40% rule + 60% AI
        return Math.round((ruleScore * 0.4f) + (aiScore * 0.6f));
    }
}

