package com.medev.hrstream.candidate.profile.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileCompletenessResponse {
    private boolean hasBasicInfo;
    private boolean hasExperience;
    private boolean hasSkills;
    private boolean hasEducation;
    private boolean hasLanguages;
    private boolean readyToApply;
}

