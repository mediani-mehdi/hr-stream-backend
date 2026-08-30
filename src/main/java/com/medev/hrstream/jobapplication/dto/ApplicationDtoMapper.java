package com.medev.hrstream.jobapplication.dto;

import com.medev.hrstream.jobapplication.JobApplication;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Converts JobApplication entities to DTOs for API responses.
 */
@Component
public class ApplicationDtoMapper {

    /**
     * Converts a JobApplication to ApplicationDetailDTO with full scoring details.
     * Used by the HR panel to display comprehensive candidate evaluation.
     */
    public ApplicationDetailDTO toDetailDTO(JobApplication app) {
        if (app == null) {
            return null;
        }

        ScoringResultDTO scoring = buildScoringResult(app);
        ApplicationDetailDTO.ScoringQuickSummary summary = buildQuickSummary(app, scoring);

        return ApplicationDetailDTO.builder()
                .applicationId(app.getId())
                .applicationDate(app.getApplicationDate())
                .status(app.getStatus())
                .candidateId(app.getCandidate().getId().toString())
                .candidateName(app.getCandidate().getFirstName() + " " + app.getCandidate().getLastName())
                .candidateEmail(app.getCandidate().getEmail())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .jobDescription(app.getJob().getDescription())
                .scoring(scoring)
                .scoringSummary(summary)
                .build();
    }

    /**
     * Builds the detailed scoring results DTO.
     */
    private ScoringResultDTO buildScoringResult(JobApplication app) {
        ScoringDetailDTO details = null;

        if (app.getRuleScoreDetails() != null && !app.getRuleScoreDetails().isEmpty()) {
            details = ScoringDetailDTO.builder()
                    .requiredSkillsMatched(getIntFromMap(app.getRuleScoreDetails(), "requiredSkillsMatched"))
                    .requiredSkillsTotal(getIntFromMap(app.getRuleScoreDetails(), "requiredSkillsTotal"))
                    .matchedRequiredSkills(getListFromMap(app.getRuleScoreDetails(), "matchedRequiredSkills"))
                    .niceSkillsMatched(getIntFromMap(app.getRuleScoreDetails(), "niceSkillsMatched"))
                    .niceSkillsTotal(getIntFromMap(app.getRuleScoreDetails(), "niceSkillsTotal"))
                    .matchedNiceSkills(getListFromMap(app.getRuleScoreDetails(), "matchedNiceSkills"))
                    .experienceKeywordHits(getIntFromMap(app.getRuleScoreDetails(), "experienceKeywordHits"))
                    .rawDetails(app.getRuleScoreDetails())
                    .build();
        }

        ScoringResultDTO result = ScoringResultDTO.builder()
                .pipelineStatus(app.getPipelineStatus())
                .processedAt(app.getProcessedAt())
                .pipelineAttemptCount(app.getPipelineAttemptCount())
                .cvExtractedChars(app.getCvExtractedChars())
                .ruleScore(app.getRuleScore())
                .ruleScoreDetails(details)
                .aiScore(app.getAiScore())
                .aiReasoning(app.getAiReasoning())
                .aiProvider(app.getAiProvider())
                .processingErrorCode(app.getProcessingErrorCode())
                .processingErrorMessage(app.getProcessingErrorMessage())
                .build();

        // Calculate overall score
        result.setOverallScore(result.calculateOverallScore());
        return result;
    }

    /**
     * Builds a quick summary for list views and quick displays.
     */
    private ApplicationDetailDTO.ScoringQuickSummary buildQuickSummary(JobApplication app, ScoringResultDTO scoring) {
        String scoringStatus = determineScoringStatus(app.getPipelineStatus());
        Integer overallScore = scoring != null ? scoring.calculateOverallScore() : null;

        return ApplicationDetailDTO.ScoringQuickSummary.builder()
                .overallScore(overallScore)
                .ruleScore(app.getRuleScore())
                .aiScore(app.getAiScore())
                .scoringStatus(scoringStatus)
                .aiProvider(app.getAiProvider())
                .build();
    }

    /**
     * Determines the user-friendly scoring status.
     */
    private String determineScoringStatus(PipelineStatus pipelineStatus) {
        if (pipelineStatus == null) {
            return "PENDING";
        }
        return switch (pipelineStatus) {
            case QUEUED, EXTRACTING, RULE_SCORING, AI_SCORING -> "PROCESSING";
            case DONE -> "DONE";
            case FAILED -> "FAILED";
        };
    }

    /**
     * Helper to extract integer from map (handles different types).
     */
    private Integer getIntFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }

    /**
     * Helper to extract list from map.
     */
    @SuppressWarnings("unchecked")
    private List<String> getListFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return null;
    }
}

