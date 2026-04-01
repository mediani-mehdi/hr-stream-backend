package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.*;
import com.medev.hrstream.candidate.profile.dto.CandidateProfileResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CandidateProfileMapper {

    public CandidateProfileResponse toResponse(Candidate candidate) {
        return CandidateProfileResponse.builder()
                .id(parseId(candidate.getId()))
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .headline(candidate.getHeadline())
                .summary(candidate.getSummary())
                .location(candidate.getLocation())
                .linkedinUrl(candidate.getLinkedinUrl())
                .niveauEtude(candidate.getNiveauEtude())
                .domaineExpertise(candidate.getDomaineExpertise())
                .experienceProfessionnelle(candidate.getExperienceProfessionnelle())
                .education(mapEducation(candidate.getEducation()))
                .experience(mapExperience(candidate.getExperience()))
                .skills(mapSkills(candidate.getSkills()))
                .languages(mapLanguages(candidate.getLanguages()))
                .build();
    }

    private List<CandidateProfileResponse.EducationResponse> mapEducation(List<CandidateEducation> source) {
        return source.stream()
                .map(item -> CandidateProfileResponse.EducationResponse.builder()
                        .id(item.getId())
                        .institution(item.getInstitution())
                        .degree(item.getDegree())
                        .fieldOfStudy(item.getFieldOfStudy())
                        .startYear(item.getStartYear())
                        .endYear(item.getEndYear())
                        .current(item.isCurrent())
                        .description(item.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CandidateProfileResponse.ExperienceResponse> mapExperience(List<CandidateExperience> source) {
        return source.stream()
                .map(item -> CandidateProfileResponse.ExperienceResponse.builder()
                        .id(item.getId())
                        .company(item.getCompany())
                        .title(item.getTitle())
                        .location(item.getLocation())
                        .startDate(item.getStartDate())
                        .endDate(item.getEndDate())
                        .current(item.isCurrent())
                        .description(item.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CandidateProfileResponse.SkillResponse> mapSkills(List<CandidateSkill> source) {
        return source.stream()
                .map(item -> CandidateProfileResponse.SkillResponse.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .level(item.getLevel())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CandidateProfileResponse.LanguageResponse> mapLanguages(List<CandidateLanguage> source) {
        return source.stream()
                .map(item -> CandidateProfileResponse.LanguageResponse.builder()
                        .id(item.getId())
                        .language(item.getLanguage())
                        .level(item.getLevel())
                        .build())
                .collect(Collectors.toList());
    }

    private UUID parseId(String id) {
        return id == null ? null : UUID.fromString(id);
    }
}

