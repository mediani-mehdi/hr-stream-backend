package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.Candidate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CandidateProfileMapper {

    public CandidateProfileResponse toProfileResponse(Candidate candidate) {
        return CandidateProfileResponse.builder()
                .id(candidate.getId())
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .headline(candidate.getHeadline())
                .summary(candidate.getSummary())
                .location(candidate.getLocation())
                .linkedinUrl(candidate.getLinkedinUrl())
                .education(toEducationResponseList(candidate.getEducation()))
                .experience(toExperienceResponseList(candidate.getExperience()))
                .skills(toSkillResponseList(candidate.getSkills()))
                .languages(toLanguageResponseList(candidate.getLanguages()))
                .build();
    }

    public List<CandidateProfileResponse.EducationResponse> toEducationResponseList(List<CandidateEducation> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toEducationResponse).toList();
    }

    public List<CandidateProfileResponse.ExperienceResponse> toExperienceResponseList(List<CandidateExperience> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toExperienceResponse).toList();
    }

    public List<CandidateProfileResponse.SkillResponse> toSkillResponseList(List<CandidateSkill> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toSkillResponse).toList();
    }

    public List<CandidateProfileResponse.LanguageResponse> toLanguageResponseList(List<CandidateLanguage> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toLanguageResponse).toList();
    }

    public CandidateProfileResponse.EducationResponse toEducationResponse(CandidateEducation entity) {
        return CandidateProfileResponse.EducationResponse.builder()
                .id(entity.getId())
                .institution(entity.getInstitution())
                .degree(entity.getDegree())
                .fieldOfStudy(entity.getFieldOfStudy())
                .startYear(entity.getStartYear())
                .endYear(entity.getEndYear())
                .isCurrent(entity.getIsCurrent())
                .description(entity.getDescription())
                .build();
    }

    public CandidateProfileResponse.ExperienceResponse toExperienceResponse(CandidateExperience entity) {
        return CandidateProfileResponse.ExperienceResponse.builder()
                .id(entity.getId())
                .company(entity.getCompany())
                .title(entity.getTitle())
                .location(entity.getLocation())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isCurrent(entity.getIsCurrent())
                .description(entity.getDescription())
                .build();
    }

    public CandidateProfileResponse.SkillResponse toSkillResponse(CandidateSkill entity) {
        return CandidateProfileResponse.SkillResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .level(entity.getLevel())
                .build();
    }

    public CandidateProfileResponse.LanguageResponse toLanguageResponse(CandidateLanguage entity) {
        return CandidateProfileResponse.LanguageResponse.builder()
                .id(entity.getId())
                .language(entity.getLanguage())
                .level(entity.getLevel())
                .build();
    }

    public CandidateEducation toEducationEntity(EducationRequest request, Candidate candidate) {
        return CandidateEducation.builder()
                .candidate(candidate)
                .institution(request.getInstitution())
                .degree(request.getDegree())
                .fieldOfStudy(request.getFieldOfStudy())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .isCurrent(request.getIsCurrent())
                .description(request.getDescription())
                .build();
    }

    public CandidateExperience toExperienceEntity(ExperienceRequest request, Candidate candidate) {
        return CandidateExperience.builder()
                .candidate(candidate)
                .company(request.getCompany())
                .title(request.getTitle())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isCurrent(request.getIsCurrent())
                .description(request.getDescription())
                .build();
    }

    public CandidateSkill toSkillEntity(SkillRequest request, Candidate candidate) {
        return CandidateSkill.builder()
                .candidate(candidate)
                .name(request.getName())
                .level(request.getLevel())
                .build();
    }

    public CandidateLanguage toLanguageEntity(LanguageRequest request, Candidate candidate) {
        return CandidateLanguage.builder()
                .candidate(candidate)
                .language(request.getLanguage())
                .level(request.getLevel())
                .build();
    }
}
