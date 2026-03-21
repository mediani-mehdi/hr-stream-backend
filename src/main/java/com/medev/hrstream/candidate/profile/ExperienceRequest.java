package com.medev.hrstream.candidate.profile;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExperienceRequest {
    private String company;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String description;
}
