package com.medev.hrstream.candidate.profile.dto;

import com.medev.hrstream.candidate.LanguageLevel;
import com.medev.hrstream.candidate.SkillLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CandidateProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String headline;
    private String summary;
    private String location;
    private String linkedinUrl;
    private String niveauEtude;
    private String domaineExpertise;
    private String experienceProfessionnelle;
    private List<EducationResponse> education;
    private List<ExperienceResponse> experience;
    private List<SkillResponse> skills;
    private List<LanguageResponse> languages;

    @Data
    @Builder
    public static class EducationResponse {
        private UUID id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private Integer startYear;
        private Integer endYear;
        private boolean current;
        private String description;
    }

    @Data
    @Builder
    public static class ExperienceResponse {
        private UUID id;
        private String company;
        private String title;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean current;
        private String description;
    }

    @Data
    @Builder
    public static class SkillResponse {
        private UUID id;
        private String name;
        private SkillLevel level;
    }

    @Data
    @Builder
    public static class LanguageResponse {
        private UUID id;
        private String language;
        private LanguageLevel level;
    }
}

