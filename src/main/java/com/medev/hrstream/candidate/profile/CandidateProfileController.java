package com.medev.hrstream.candidate.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidate/profile")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "Candidate Profile", description = "Manage the authenticated candidate's profile")
public class CandidateProfileController {

    private final CandidateProfileService profileService;

    public CandidateProfileController(CandidateProfileService profileService) {
        this.profileService = profileService;
    }

    // ── Profile ──────────────────────────────────────────────────────

    @Operation(summary = "Get full candidate profile")
    @GetMapping
    public ResponseEntity<CandidateProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profileService.getProfile(candidateId));
    }

    @Operation(summary = "Update basic profile information")
    @PutMapping("/basic-info")
    public ResponseEntity<CandidateProfileResponse> updateBasicInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateBasicInfoRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profileService.updateBasicInfo(candidateId, request));
    }

    @Operation(summary = "Check profile completeness and readiness to apply")
    @GetMapping("/completeness")
    public ResponseEntity<ProfileCompletenessResponse> getCompleteness(
            @AuthenticationPrincipal UserDetails userDetails) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profileService.getCompleteness(candidateId));
    }

    // ── Education ────────────────────────────────────────────────────

    @Operation(summary = "Add an education record")
    @PostMapping("/education")
    public ResponseEntity<CandidateProfileResponse.EducationResponse> addEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody EducationRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addEducation(candidateId, request));
    }

    @Operation(summary = "Update an education record")
    @PutMapping("/education/{educationId}")
    public ResponseEntity<CandidateProfileResponse.EducationResponse> updateEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String educationId,
            @RequestBody EducationRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profileService.updateEducation(candidateId, educationId, request));
    }

    @Operation(summary = "Delete an education record")
    @DeleteMapping("/education/{educationId}")
    public ResponseEntity<Void> deleteEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String educationId) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        profileService.deleteEducation(candidateId, educationId);
        return ResponseEntity.noContent().build();
    }

    // ── Experience ───────────────────────────────────────────────────

    @Operation(summary = "Add a work-experience record")
    @PostMapping("/experience")
    public ResponseEntity<CandidateProfileResponse.ExperienceResponse> addExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ExperienceRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addExperience(candidateId, request));
    }

    @Operation(summary = "Update a work-experience record")
    @PutMapping("/experience/{experienceId}")
    public ResponseEntity<CandidateProfileResponse.ExperienceResponse> updateExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String experienceId,
            @RequestBody ExperienceRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profileService.updateExperience(candidateId, experienceId, request));
    }

    @Operation(summary = "Delete a work-experience record")
    @DeleteMapping("/experience/{experienceId}")
    public ResponseEntity<Void> deleteExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String experienceId) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        profileService.deleteExperience(candidateId, experienceId);
        return ResponseEntity.noContent().build();
    }

    // ── Skills ───────────────────────────────────────────────────────

    @Operation(summary = "Add a skill")
    @PostMapping("/skills")
    public ResponseEntity<CandidateProfileResponse.SkillResponse> addSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SkillRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addSkill(candidateId, request));
    }

    @Operation(summary = "Update a skill")
    @PutMapping("/skills/{skillId}")
    public ResponseEntity<CandidateProfileResponse.SkillResponse> updateSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String skillId,
            @RequestBody SkillRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profileService.updateSkill(candidateId, skillId, request));
    }

    @Operation(summary = "Delete a skill")
    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String skillId) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        profileService.deleteSkill(candidateId, skillId);
        return ResponseEntity.noContent().build();
    }

    // ── Languages ────────────────────────────────────────────────────

    @Operation(summary = "Add a language proficiency")
    @PostMapping("/languages")
    public ResponseEntity<CandidateProfileResponse.LanguageResponse> addLanguage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LanguageRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addLanguage(candidateId, request));
    }

    @Operation(summary = "Update a language proficiency")
    @PutMapping("/languages/{languageId}")
    public ResponseEntity<CandidateProfileResponse.LanguageResponse> updateLanguage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String languageId,
            @RequestBody LanguageRequest request) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profileService.updateLanguage(candidateId, languageId, request));
    }

    @Operation(summary = "Delete a language proficiency")
    @DeleteMapping("/languages/{languageId}")
    public ResponseEntity<Void> deleteLanguage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String languageId) {
        String candidateId = profileService.getCandidateIdByEmail(userDetails.getUsername());
        profileService.deleteLanguage(candidateId, languageId);
        return ResponseEntity.noContent().build();
    }
}
