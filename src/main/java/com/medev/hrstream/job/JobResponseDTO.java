package com.medev.hrstream.job;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JobResponseDTO {
    private String id;
    private String title;
    private String description;
    private String applicationLink;
    private JobStatus status;
    private String location;
    private String experienceLevel;
    private String employmentType;
    private List<String> skills;
    private LocalDateTime createdAt;
}