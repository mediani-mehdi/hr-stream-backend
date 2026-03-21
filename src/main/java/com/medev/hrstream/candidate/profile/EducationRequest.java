package com.medev.hrstream.candidate.profile;

import lombok.Data;

@Data
public class EducationRequest {
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    private Boolean isCurrent;
    private String description;
}
