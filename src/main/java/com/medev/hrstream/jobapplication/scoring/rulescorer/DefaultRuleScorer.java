package com.medev.hrstream.jobapplication.scoring.rulescorer;

import com.medev.hrstream.config.ScoringProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DefaultRuleScorer implements RuleScorer {

    private static final List<String> EXPERIENCE_KEYWORDS = List.of(
            "junior", "mid", "senior", "lead", "principal", "staff"
    );

    private final ScoringProperties props;

    public DefaultRuleScorer(ScoringProperties props) {
        this.props = props;
    }

    @Override
    public RuleScoreResult score(RuleScorerInput input) {
        String cv = input.getCvText() == null ? "" : input.getCvText().toLowerCase(Locale.ROOT);
        List<String> required = input.getRequiredSkills() == null ? List.of() : input.getRequiredSkills();
        List<String> nice = input.getNiceToHaveSkills() == null ? List.of() : input.getNiceToHaveSkills();

        List<String> matchedRequired = new ArrayList<>();
        for (String skill : required) {
            if (skill != null && !skill.isBlank() && cv.contains(skill.toLowerCase(Locale.ROOT))) {
                matchedRequired.add(skill);
            }
        }
        List<String> matchedNice = new ArrayList<>();
        for (String skill : nice) {
            if (skill != null && !skill.isBlank() && cv.contains(skill.toLowerCase(Locale.ROOT))) {
                matchedNice.add(skill);
            }
        }

        int expHits = countExperienceHits(cv, input.getExperienceLevel());

        int requiredScore = required.isEmpty()
                ? 0
                : (int) Math.floor((matchedRequired.size() * 1.0 / required.size()) * props.getRequiredSkillWeight());
        int niceScore = nice.isEmpty()
                ? 0
                : (int) Math.floor((matchedNice.size() * 1.0 / nice.size()) * props.getNiceSkillWeight());
        int expScore = expHits > 0 ? props.getExperienceKeywordWeight() : 0;

        int total = (required.isEmpty() ? props.getRequiredSkillWeight() : requiredScore)
                + niceScore + expScore;
        total = Math.min(100, Math.max(0, total));

        RuleScoreDetail detail = RuleScoreDetail.builder()
                .requiredSkillsMatched(matchedRequired.size())
                .requiredSkillsTotal(required.size())
                .matchedRequiredSkills(matchedRequired)
                .niceSkillsMatched(matchedNice.size())
                .niceSkillsTotal(nice.size())
                .matchedNiceSkills(matchedNice)
                .experienceKeywordHits(expHits)
                .requiredComponentScore(required.isEmpty() ? props.getRequiredSkillWeight() : requiredScore)
                .niceComponentScore(niceScore)
                .experienceComponentScore(expScore)
                .build();

        return RuleScoreResult.builder().score(total).detail(detail).build();
    }

    private int countExperienceHits(String cv, String level) {
        int hits = 0;
        if (level != null) {
            String lvl = level.toLowerCase(Locale.ROOT);
            if (cv.contains(lvl)) {
                hits++;
            }
        }
        for (String kw : EXPERIENCE_KEYWORDS) {
            if (cv.contains(kw)) {
                hits++;
            }
        }
        return hits;
    }
}
