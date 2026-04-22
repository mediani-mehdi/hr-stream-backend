package com.medev.hrstream.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scoring")
@Data
public class ScoringProperties {

    /** Minimum rule score (0-100) a CV must reach to proceed to AI scoring. */
    private int ruleMinScore = 40;

    /** Minimum AI score (0-100) to keep status = SUBMITTED; lower auto-rejects. */
    private int aiMinScore = 60;

    /** Fixed cap on non-rejected applications per job. */
    private int maxApplications = 100;

    /** Weights for DefaultRuleScorer (sum to 100). */
    private int requiredSkillWeight = 60;
    private int niceSkillWeight = 25;
    private int experienceKeywordWeight = 15;

    /** Maximum characters of extracted PDF text to feed to the AI prompt. */
    private int promptCharBudget = 20000;

    /** Minimum extracted characters to consider a CV non-empty. */
    private int minExtractedChars = 50;

    /** How long an application may stay in EXTRACTING/RULE_SCORING/AI_SCORING before StuckApplicationResumer re-queues it. */
    private long stuckAfterMinutes = 15;
}
