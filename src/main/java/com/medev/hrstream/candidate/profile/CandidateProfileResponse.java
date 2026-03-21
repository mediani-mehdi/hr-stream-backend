package com.medev.hrstream.candidate.profile;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CandidateProfileResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String headline;
    private String summary;
    private String location;
    private String linkedinUrl;

    private List<EducationResponse> education;
    private List<ExperienceResponse> experience;
    private List<SkillResponse> skills;
    private List<LanguageResponse> languages;

    @Data
    @Builder
    public static class EducationResponse {
        private String id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private Integer startYear;
        private Integer endYear;
        private Boolean isCurrent;
        private String description;
    }

    @Data
    @Builder
    public static class ExperienceResponse {
        private String id;
        private String company;
        private String title;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private String description;
    }

    @Data
    @Builder
    public static class SkillResponse {
        private String id;
        private String name;
        private SkillLevel level;
    }

    @Data
    @Builder
    public static class LanguageResponse {
        private String id;
        private String language;
        private LanguageLevel level;
    }
}
