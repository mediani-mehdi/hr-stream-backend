package com.medev.hrstream.candidate.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EducationRequest {

    @NotBlank
    private String institution;

    private String degree;
    private String fieldOfStudy;

    private Integer startYear;
    private Integer endYear;

    private boolean current;

    @Size(max = 2000)
    private String description;
}

