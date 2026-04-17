package com.medev.hrstream.jobapplication.scoring.rulescorer;

public interface RuleScorer {
    RuleScoreResult score(RuleScorerInput input);
}
