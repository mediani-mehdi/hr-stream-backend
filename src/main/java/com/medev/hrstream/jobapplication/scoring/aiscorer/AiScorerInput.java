package com.medev.hrstream.jobapplication.scoring.aiscorer;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AiScorerInput {
    String jobTitle;
    String jobDescription;
    List<String> requiredSkills;
    List<String> niceToHaveSkills;
    String experienceLevel;
    String cvText;
}
