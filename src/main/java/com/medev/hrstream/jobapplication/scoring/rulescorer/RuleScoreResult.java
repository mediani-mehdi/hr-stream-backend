package com.medev.hrstream.jobapplication.scoring.rulescorer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RuleScoreResult {
    int score;
    RuleScoreDetail detail;
}
