package com.medev.hrstream.candidate.profile.dto;

import com.medev.hrstream.candidate.LanguageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LanguageRequest {

    @NotBlank
    private String language;

    @NotNull
    private LanguageLevel level;
}

