package com.medev.hrstream.candidate.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileCompletenessResponse {
    private boolean hasBasicInfo;
    private boolean hasEducation;
    private boolean hasExperience;
    private boolean hasSkills;
    private boolean hasLanguages;
    private boolean readyToApply;
}
