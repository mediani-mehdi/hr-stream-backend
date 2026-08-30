package com.medev.hrstream.jobapplication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Detailed breakdown of the rule-based scoring results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScoringDetailDTO {

    // Required skills matching
    private Integer requiredSkillsMatched;
    private Integer requiredSkillsTotal;
    private List<String> matchedRequiredSkills;

    // Nice-to-have skills matching
    private Integer niceSkillsMatched;
    private Integer niceSkillsTotal;
    private List<String> matchedNiceSkills;

    // Experience level indicators
    private Integer experienceKeywordHits;

    // Additional metadata
    private Map<String, Object> rawDetails;
}

