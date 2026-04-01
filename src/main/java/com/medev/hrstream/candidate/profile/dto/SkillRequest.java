package com.medev.hrstream.candidate.profile.dto;

import com.medev.hrstream.candidate.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillRequest {

    @NotBlank
    private String name;

    @NotNull
    private SkillLevel level;
}

