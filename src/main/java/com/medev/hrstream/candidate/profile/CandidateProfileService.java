package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CandidateProfileService {

    private final CandidateRepository candidateRepository;
    private final CandidateEducationRepository educationRepository;
    private final CandidateExperienceRepository experienceRepository;
    private final CandidateSkillRepository skillRepository;
    private final CandidateLanguageRepository languageRepository;
    private final CandidateProfileMapper mapper;

    public CandidateProfileService(
            CandidateRepository candidateRepository,
            CandidateEducationRepository educationRepository,
            CandidateExperienceRepository experienceRepository,
            CandidateSkillRepository skillRepository,
            CandidateLanguageRepository languageRepository,
            CandidateProfileMapper mapper) {
        this.candidateRepository = candidateRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.languageRepository = languageRepository;
        this.mapper = mapper;
    }

    public String getCandidateIdByEmail(String email) {
        return findCandidateByEmail(email).getId();
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfile(String candidateId) {
        Candidate candidate = findCandidateById(candidateId);
        return mapper.toProfileResponse(candidate);
    }

    @Transactional
    public CandidateProfileResponse updateBasicInfo(String candidateId, UpdateBasicInfoRequest request) {
        Candidate candidate = findCandidateById(candidateId);
        if (request.getFirstName() != null) candidate.setFirstName(request.getFirstName());
        if (request.getLastName() != null) candidate.setLastName(request.getLastName());
        if (request.getPhone() != null) candidate.setPhone(request.getPhone());
        if (request.getHeadline() != null) candidate.setHeadline(request.getHeadline());
        if (request.getSummary() != null) candidate.setSummary(request.getSummary());
        if (request.getLocation() != null) candidate.setLocation(request.getLocation());
        if (request.getLinkedinUrl() != null) candidate.setLinkedinUrl(request.getLinkedinUrl());
        candidateRepository.save(candidate);
        return mapper.toProfileResponse(candidate);
    }

    // ── Education ────────────────────────────────────────────────────

    @Transactional
    public CandidateProfileResponse.EducationResponse addEducation(String candidateId, EducationRequest request) {
        Candidate candidate = findCandidateById(candidateId);
        CandidateEducation education = mapper.toEducationEntity(request, candidate);
        return mapper.toEducationResponse(educationRepository.save(education));
    }

    @Transactional
    public CandidateProfileResponse.EducationResponse updateEducation(String candidateId, String educationId, EducationRequest request) {
        CandidateEducation education = findEducationByIdAndCandidate(educationId, candidateId);
        education.setInstitution(request.getInstitution());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartYear(request.getStartYear());
        education.setEndYear(request.getEndYear());
        education.setIsCurrent(request.getIsCurrent());
        education.setDescription(request.getDescription());
        return mapper.toEducationResponse(educationRepository.save(education));
    }

    @Transactional
    public void deleteEducation(String candidateId, String educationId) {
        CandidateEducation education = findEducationByIdAndCandidate(educationId, candidateId);
        educationRepository.delete(education);
    }

    // ── Experience ───────────────────────────────────────────────────

    @Transactional
    public CandidateProfileResponse.ExperienceResponse addExperience(String candidateId, ExperienceRequest request) {
        Candidate candidate = findCandidateById(candidateId);
        CandidateExperience experience = mapper.toExperienceEntity(request, candidate);
        return mapper.toExperienceResponse(experienceRepository.save(experience));
    }

    @Transactional
    public CandidateProfileResponse.ExperienceResponse updateExperience(String candidateId, String experienceId, ExperienceRequest request) {
        CandidateExperience experience = findExperienceByIdAndCandidate(experienceId, candidateId);
        experience.setCompany(request.getCompany());
        experience.setTitle(request.getTitle());
        experience.setLocation(request.getLocation());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setIsCurrent(request.getIsCurrent());
        experience.setDescription(request.getDescription());
        return mapper.toExperienceResponse(experienceRepository.save(experience));
    }

    @Transactional
    public void deleteExperience(String candidateId, String experienceId) {
        CandidateExperience experience = findExperienceByIdAndCandidate(experienceId, candidateId);
        experienceRepository.delete(experience);
    }

    // ── Skills ───────────────────────────────────────────────────────

    @Transactional
    public CandidateProfileResponse.SkillResponse addSkill(String candidateId, SkillRequest request) {
        Candidate candidate = findCandidateById(candidateId);
        CandidateSkill skill = mapper.toSkillEntity(request, candidate);
        return mapper.toSkillResponse(skillRepository.save(skill));
    }

    @Transactional
    public CandidateProfileResponse.SkillResponse updateSkill(String candidateId, String skillId, SkillRequest request) {
        CandidateSkill skill = findSkillByIdAndCandidate(skillId, candidateId);
        skill.setName(request.getName());
        skill.setLevel(request.getLevel());
        return mapper.toSkillResponse(skillRepository.save(skill));
    }

    @Transactional
    public void deleteSkill(String candidateId, String skillId) {
        CandidateSkill skill = findSkillByIdAndCandidate(skillId, candidateId);
        skillRepository.delete(skill);
    }

    // ── Languages ────────────────────────────────────────────────────

    @Transactional
    public CandidateProfileResponse.LanguageResponse addLanguage(String candidateId, LanguageRequest request) {
        Candidate candidate = findCandidateById(candidateId);
        CandidateLanguage language = mapper.toLanguageEntity(request, candidate);
        return mapper.toLanguageResponse(languageRepository.save(language));
    }

    @Transactional
    public CandidateProfileResponse.LanguageResponse updateLanguage(String candidateId, String languageId, LanguageRequest request) {
        CandidateLanguage language = findLanguageByIdAndCandidate(languageId, candidateId);
        language.setLanguage(request.getLanguage());
        language.setLevel(request.getLevel());
        return mapper.toLanguageResponse(languageRepository.save(language));
    }

    @Transactional
    public void deleteLanguage(String candidateId, String languageId) {
        CandidateLanguage language = findLanguageByIdAndCandidate(languageId, candidateId);
        languageRepository.delete(language);
    }

    // ── Completeness ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProfileCompletenessResponse getCompleteness(String candidateId) {
        Candidate candidate = findCandidateById(candidateId);
        boolean hasBasicInfo = candidate.getFirstName() != null && !candidate.getFirstName().isBlank()
                && candidate.getLastName() != null && !candidate.getLastName().isBlank();
        boolean hasEducation = !candidate.getEducation().isEmpty();
        boolean hasExperience = !candidate.getExperience().isEmpty();
        boolean hasSkills = !candidate.getSkills().isEmpty();
        boolean hasLanguages = !candidate.getLanguages().isEmpty();
        return ProfileCompletenessResponse.builder()
                .hasBasicInfo(hasBasicInfo)
                .hasEducation(hasEducation)
                .hasExperience(hasExperience)
                .hasSkills(hasSkills)
                .hasLanguages(hasLanguages)
                .readyToApply(hasExperience && hasSkills)
                .build();
    }

    public boolean isProfileComplete(String candidateId) {
        return getCompleteness(candidateId).isReadyToApply();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private Candidate findCandidateById(String candidateId) {
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
    }

    private Candidate findCandidateByEmail(String email) {
        return candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
    }

    private CandidateEducation findEducationByIdAndCandidate(String educationId, String candidateId) {
        CandidateEducation edu = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Education record not found"));
        if (!edu.getCandidate().getId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return edu;
    }

    private CandidateExperience findExperienceByIdAndCandidate(String experienceId, String candidateId) {
        CandidateExperience exp = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experience record not found"));
        if (!exp.getCandidate().getId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return exp;
    }

    private CandidateSkill findSkillByIdAndCandidate(String skillId, String candidateId) {
        CandidateSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill record not found"));
        if (!skill.getCandidate().getId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return skill;
    }

    private CandidateLanguage findLanguageByIdAndCandidate(String languageId, String candidateId) {
        CandidateLanguage lang = languageRepository.findById(languageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Language record not found"));
        if (!lang.getCandidate().getId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return lang;
    }
}
