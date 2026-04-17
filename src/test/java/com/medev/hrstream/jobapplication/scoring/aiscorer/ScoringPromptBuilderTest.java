package com.medev.hrstream.jobapplication.scoring.aiscorer;

import com.medev.hrstream.config.ScoringProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringPromptBuilderTest {

    private final ScoringPromptBuilder builder = new ScoringPromptBuilder(defaultProps());

    @Test
    void promptIncludesJobFieldsAndSchemaInstruction() {
        AiScorerInput input = AiScorerInput.builder()
                .jobTitle("Senior Java Engineer")
                .jobDescription("Build APIs")
                .requiredSkills(List.of("Java"))
                .niceToHaveSkills(List.of("Kafka"))
                .experienceLevel("senior")
                .cvText("10 yrs Java")
                .build();

        String prompt = builder.build(input);

        assertThat(prompt)
                .contains("Senior Java Engineer")
                .contains("Build APIs")
                .contains("Java")
                .contains("Kafka")
                .contains("senior")
                .contains("10 yrs Java")
                .contains("\"score\"")
                .contains("\"reasoning\"")
                .contains("0 and 100");
    }

    @Test
    void truncatesCvTextToCharBudget() {
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 40_000; i++) big.append('x');

        AiScorerInput input = AiScorerInput.builder()
                .jobTitle("x").jobDescription("x")
                .requiredSkills(List.of()).niceToHaveSkills(List.of())
                .experienceLevel(null).cvText(big.toString())
                .build();

        ScoringProperties props = new ScoringProperties();
        props.setPromptCharBudget(1000);
        String prompt = new ScoringPromptBuilder(props).build(input);

        assertThat(prompt.length()).isLessThan(5000);
        assertThat(prompt).contains("[truncated]");
    }

    @Test
    void handlesNullOptionalLists() {
        AiScorerInput input = AiScorerInput.builder()
                .jobTitle("T").jobDescription("D")
                .requiredSkills(null).niceToHaveSkills(null)
                .experienceLevel(null).cvText("C")
                .build();

        String prompt = builder.build(input);
        assertThat(prompt).contains("T").contains("D").contains("C");
    }

    private static ScoringProperties defaultProps() {
        ScoringProperties p = new ScoringProperties();
        p.setPromptCharBudget(20_000);
        return p;
    }
}
