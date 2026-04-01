package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.*;
import com.medev.hrstream.candidate.profile.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CandidateProfileService {

    private final CandidateRepository candidateRepository;
    private final CandidateProfileMapper mapper;

    public CandidateProfileService(CandidateRepository candidateRepository, CandidateProfileMapper mapper) {
        this.candidateRepository = candidateRepository;
        this.mapper = mapper;
    }

    public CandidateProfileResponse getProfile(UUID candidateId) {
        return mapper.toResponse(findCandidate(candidateId));
    }

    public CandidateProfileResponse updateBasicInfo(UUID candidateId, UpdateBasicInfoRequest request) {
        Candidate candidate = findCandidate(candidateId);
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setHeadline(request.getHeadline());
        candidate.setSummary(request.getSummary());
        candidate.setLocation(request.getLocation());
        candidate.setLinkedinUrl(request.getLinkedinUrl());
        candidate.setNiveauEtude(request.getNiveauEtude());
        candidate.setDomaineExpertise(request.getDomaineExpertise());
        candidate.setExperienceProfessionnelle(request.getExperienceProfessionnelle());
        return mapper.toResponse(candidateRepository.save(candidate));
    }

    public CandidateProfileResponse addEducation(UUID candidateId, EducationRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateEducation education = CandidateEducation.builder()
                .candidate(candidate)
                .institution(request.getInstitution())
                .degree(request.getDegree())
                .fieldOfStudy(request.getFieldOfStudy())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .current(request.isCurrent())
                .description(request.getDescription())
                .build();
        candidate.getEducation().add(education);
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public CandidateProfileResponse updateEducation(UUID candidateId, UUID educationId, EducationRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateEducation education = candidate.getEducation().stream()
                .filter(item -> item.getId().equals(educationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Education entry not found"));
        education.setInstitution(request.getInstitution());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartYear(request.getStartYear());
        education.setEndYear(request.getEndYear());
        education.setCurrent(request.isCurrent());
        education.setDescription(request.getDescription());
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public void deleteEducation(UUID candidateId, UUID educationId) {
        Candidate candidate = findCandidate(candidateId);
        boolean removed = candidate.getEducation().removeIf(item -> item.getId().equals(educationId));
        if (!removed) {
            throw new IllegalArgumentException("Education entry not found");
        }
        candidateRepository.save(candidate);
    }

    public CandidateProfileResponse addExperience(UUID candidateId, ExperienceRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateExperience experience = CandidateExperience.builder()
                .candidate(candidate)
                .company(request.getCompany())
                .title(request.getTitle())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .current(request.isCurrent())
                .description(request.getDescription())
                .build();
        candidate.getExperience().add(experience);
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public CandidateProfileResponse updateExperience(UUID candidateId, UUID experienceId, ExperienceRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateExperience experience = candidate.getExperience().stream()
                .filter(item -> item.getId().equals(experienceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Experience entry not found"));
        experience.setCompany(request.getCompany());
        experience.setTitle(request.getTitle());
        experience.setLocation(request.getLocation());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setCurrent(request.isCurrent());
        experience.setDescription(request.getDescription());
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public void deleteExperience(UUID candidateId, UUID experienceId) {
        Candidate candidate = findCandidate(candidateId);
        boolean removed = candidate.getExperience().removeIf(item -> item.getId().equals(experienceId));
        if (!removed) {
            throw new IllegalArgumentException("Experience entry not found");
        }
        candidateRepository.save(candidate);
    }

    public CandidateProfileResponse addSkill(UUID candidateId, SkillRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateSkill skill = CandidateSkill.builder()
                .candidate(candidate)
                .name(request.getName())
                .level(request.getLevel())
                .build();
        candidate.getSkills().add(skill);
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public CandidateProfileResponse updateSkill(UUID candidateId, UUID skillId, SkillRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateSkill skill = candidate.getSkills().stream()
                .filter(item -> item.getId().equals(skillId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        skill.setName(request.getName());
        skill.setLevel(request.getLevel());
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public void deleteSkill(UUID candidateId, UUID skillId) {
        Candidate candidate = findCandidate(candidateId);
        boolean removed = candidate.getSkills().removeIf(item -> item.getId().equals(skillId));
        if (!removed) {
            throw new IllegalArgumentException("Skill not found");
        }
        candidateRepository.save(candidate);
    }

    public CandidateProfileResponse addLanguage(UUID candidateId, LanguageRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateLanguage language = CandidateLanguage.builder()
                .candidate(candidate)
                .language(request.getLanguage())
                .level(request.getLevel())
                .build();
        candidate.getLanguages().add(language);
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public CandidateProfileResponse updateLanguage(UUID candidateId, UUID languageId, LanguageRequest request) {
        Candidate candidate = findCandidate(candidateId);
        CandidateLanguage language = candidate.getLanguages().stream()
                .filter(item -> item.getId().equals(languageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Language not found"));
        language.setLanguage(request.getLanguage());
        language.setLevel(request.getLevel());
        candidateRepository.save(candidate);
        return mapper.toResponse(candidate);
    }

    public void deleteLanguage(UUID candidateId, UUID languageId) {
        Candidate candidate = findCandidate(candidateId);
        boolean removed = candidate.getLanguages().removeIf(item -> item.getId().equals(languageId));
        if (!removed) {
            throw new IllegalArgumentException("Language not found");
        }
        candidateRepository.save(candidate);
    }

    public boolean isProfileComplete(UUID candidateId) {
        Candidate candidate = findCandidate(candidateId);
        return !candidate.getExperience().isEmpty() && !candidate.getSkills().isEmpty();
    }

    public ProfileCompletenessResponse getCompleteness(UUID candidateId) {
        Candidate candidate = findCandidate(candidateId);
        boolean hasExperience = !candidate.getExperience().isEmpty();
        boolean hasSkills = !candidate.getSkills().isEmpty();
        return ProfileCompletenessResponse.builder()
                .hasBasicInfo(candidate.getFirstName() != null && candidate.getLastName() != null)
                .hasEducation(!candidate.getEducation().isEmpty())
                .hasExperience(hasExperience)
                .hasSkills(hasSkills)
                .hasLanguages(!candidate.getLanguages().isEmpty())
                .readyToApply(hasExperience && hasSkills)
                .build();
    }

    private Candidate findCandidate(UUID candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId.toString())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
        ensureCollections(candidate);
        return candidate;
    }

    private void ensureCollections(Candidate candidate) {
        if (candidate.getEducation() == null) {
            candidate.setEducation(new java.util.ArrayList<>());
        }
        if (candidate.getExperience() == null) {
            candidate.setExperience(new java.util.ArrayList<>());
        }
        if (candidate.getSkills() == null) {
            candidate.setSkills(new java.util.ArrayList<>());
        }
        if (candidate.getLanguages() == null) {
            candidate.setLanguages(new java.util.ArrayList<>());
        }
    }
}
