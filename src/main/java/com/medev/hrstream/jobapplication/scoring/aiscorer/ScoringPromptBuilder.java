package com.medev.hrstream.jobapplication.scoring.aiscorer;

import com.medev.hrstream.config.ScoringProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScoringPromptBuilder {

    private final ScoringProperties props;

    public ScoringPromptBuilder(ScoringProperties props) {
        this.props = props;
    }

    public String build(AiScorerInput input) {
        String cv = truncate(nullToEmpty(input.getCvText()), props.getPromptCharBudget());
        return """
                You are an ATS scorer. Evaluate how well the candidate's CV fits the job.
                Output STRICT JSON only, matching this schema:
                { "score": <integer between 0 and 100>, "reasoning": "<short paragraph>" }
                Do not include any prose outside the JSON object.

                ## Job
                Title: %s
                Description: %s
                Required skills: %s
                Nice-to-have skills: %s
                Experience level: %s

                ## Candidate CV
                %s
                """.formatted(
                        nullToEmpty(input.getJobTitle()),
                        nullToEmpty(input.getJobDescription()),
                        formatList(input.getRequiredSkills()),
                        formatList(input.getNiceToHaveSkills()),
                        nullToEmpty(input.getExperienceLevel()),
                        cv
        );
    }

    private String truncate(String text, int budget) {
        if (text.length() <= budget) return text;
        return text.substring(0, budget) + "\n[truncated]";
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) return "(none)";
        return String.join(", ", list);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
