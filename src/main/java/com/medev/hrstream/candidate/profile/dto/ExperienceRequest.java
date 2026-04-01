package com.medev.hrstream.candidate.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExperienceRequest {

    @NotBlank
    private String company;

    @NotBlank
    private String title;

    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;

    @Size(max = 4000)
    private String description;
}

