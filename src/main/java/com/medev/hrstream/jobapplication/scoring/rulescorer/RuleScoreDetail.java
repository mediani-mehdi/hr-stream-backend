package com.medev.hrstream.jobapplication.scoring.rulescorer;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RuleScoreDetail {
    int requiredSkillsMatched;
    int requiredSkillsTotal;
    List<String> matchedRequiredSkills;
    int niceSkillsMatched;
    int niceSkillsTotal;
    List<String> matchedNiceSkills;
    int experienceKeywordHits;
    int requiredComponentScore;
    int niceComponentScore;
    int experienceComponentScore;
}
