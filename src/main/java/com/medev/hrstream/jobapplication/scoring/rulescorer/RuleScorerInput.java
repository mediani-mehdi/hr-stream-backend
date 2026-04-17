package com.medev.hrstream.jobapplication.scoring.rulescorer;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RuleScorerInput {
    String cvText;
    List<String> requiredSkills;
    List<String> niceToHaveSkills;
    String experienceLevel;
}
