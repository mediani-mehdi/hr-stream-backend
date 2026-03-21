package com.medev.hrstream.job;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PublicJobResponse {
    private String id;
    private String title;
    private String description;
    private String location;
    private String experienceLevel;
    private String employmentType;
    private JobStatus status;
    private List<String> skills;
    private LocalDateTime createdAt;
    private String applyUrl;
}
