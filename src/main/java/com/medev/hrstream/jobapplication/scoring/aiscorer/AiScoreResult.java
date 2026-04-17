package com.medev.hrstream.jobapplication.scoring.aiscorer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AiScoreResult {
    int score;
    String reasoning;
    String providerName;
}
