package com.medev.hrstream.jobapplication.scoring.rulescorer;

import com.medev.hrstream.config.ScoringProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRuleScorerTest {

    private DefaultRuleScorer scorer;

    @BeforeEach
    void init() {
        ScoringProperties props = new ScoringProperties();
        props.setRequiredSkillWeight(60);
        props.setNiceSkillWeight(25);
        props.setExperienceKeywordWeight(15);
        scorer = new DefaultRuleScorer(props);
    }

    @Test
    void returnsZeroWhenNoRequiredSkillsMatch() {
        RuleScorerInput input = RuleScorerInput.builder()
                .cvText("I enjoy cooking and hiking.")
                .requiredSkills(List.of("Java", "Spring"))
                .niceToHaveSkills(List.of("Kubernetes"))
                .experienceLevel("senior")
                .build();

        RuleScoreResult result = scorer.score(input);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getDetail().getRequiredSkillsMatched()).isZero();
    }

    @Test
    void matchesRequiredSkillsCaseInsensitively() {
        RuleScorerInput input = RuleScorerInput.builder()
                .cvText("Built REST APIs with JAVA and spring boot.")
                .requiredSkills(List.of("Java", "Spring"))
                .niceToHaveSkills(List.of())
                .experienceLevel(null)
                .build();

        RuleScoreResult result = scorer.score(input);

        assertThat(result.getDetail().getRequiredSkillsMatched()).isEqualTo(2);
        assertThat(result.getDetail().getMatchedRequiredSkills())
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    void fullMatchOnAllComponentsYields100() {
        RuleScorerInput input = RuleScorerInput.builder()
                .cvText("10 years senior Java Spring Kubernetes experience leading teams.")
                .requiredSkills(List.of("Java", "Spring"))
                .niceToHaveSkills(List.of("Kubernetes"))
                .experienceLevel("senior")
                .build();

        RuleScoreResult result = scorer.score(input);

        assertThat(result.getScore()).isEqualTo(100);
    }

    @Test
    void partialMatchProportional() {
        RuleScorerInput input = RuleScorerInput.builder()
                .cvText("Java developer with some AWS background.")
                .requiredSkills(List.of("Java", "Spring"))   // 1/2 matched -> 30 points
                .niceToHaveSkills(List.of("AWS", "GCP"))     // 1/2 matched -> 12 points (floor)
                .experienceLevel(null)
                .build();

        RuleScoreResult result = scorer.score(input);

        // 30 (required 50%) + 12 (nice 50% floored) + 0 (no level) = 42
        assertThat(result.getScore()).isBetween(40, 45);
        assertThat(result.getDetail().getRequiredSkillsMatched()).isEqualTo(1);
        assertThat(result.getDetail().getNiceSkillsMatched()).isEqualTo(1);
    }

    @Test
    void handlesNullNiceSkillsList() {
        RuleScorerInput input = RuleScorerInput.builder()
                .cvText("Java everywhere")
                .requiredSkills(List.of("Java"))
                .niceToHaveSkills(null)
                .experienceLevel(null)
                .build();

        RuleScoreResult result = scorer.score(input);

        assertThat(result.getScore()).isEqualTo(60);    // full required weight
        assertThat(result.getDetail().getNiceSkillsTotal()).isZero();
    }
}
