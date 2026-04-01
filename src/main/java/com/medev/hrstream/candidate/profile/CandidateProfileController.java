package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.CandidateIdentityService;
import com.medev.hrstream.candidate.profile.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/candidate/profile")
@Validated
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;
    private final CandidateIdentityService candidateIdentityService;

    public CandidateProfileController(CandidateProfileService candidateProfileService, CandidateIdentityService candidateIdentityService) {
        this.candidateProfileService = candidateProfileService;
        this.candidateIdentityService = candidateIdentityService;
    }

    @Operation(summary = "Get the authenticated candidate profile")
    @GetMapping
    public ResponseEntity<CandidateProfileResponse> getProfile() {
        return ResponseEntity.ok(candidateProfileService.getProfile(currentCandidateId()));
    }

    @Operation(summary = "Update basic profile information")
    @PutMapping("/basic-info")
    public ResponseEntity<CandidateProfileResponse> updateBasicInfo(@Valid @RequestBody UpdateBasicInfoRequest request) {
        CandidateProfileResponse response = candidateProfileService.updateBasicInfo(currentCandidateId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add education entry")
    @PostMapping("/education")
    public ResponseEntity<CandidateProfileResponse> addEducation(@Valid @RequestBody EducationRequest request) {
        CandidateProfileResponse response = candidateProfileService.addEducation(currentCandidateId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update education entry")
    @PutMapping("/education/{educationId}")
    public ResponseEntity<CandidateProfileResponse> updateEducation(@PathVariable UUID educationId,
                                                                    @Valid @RequestBody EducationRequest request) {
        CandidateProfileResponse response = candidateProfileService.updateEducation(currentCandidateId(), educationId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete education entry")
    @DeleteMapping("/education/{educationId}")
    public ResponseEntity<Void> deleteEducation(@PathVariable UUID educationId) {
        candidateProfileService.deleteEducation(currentCandidateId(), educationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add experience entry")
    @PostMapping("/experience")
    public ResponseEntity<CandidateProfileResponse> addExperience(@Valid @RequestBody ExperienceRequest request) {
        CandidateProfileResponse response = candidateProfileService.addExperience(currentCandidateId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update experience entry")
    @PutMapping("/experience/{experienceId}")
    public ResponseEntity<CandidateProfileResponse> updateExperience(@PathVariable UUID experienceId,
                                                                     @Valid @RequestBody ExperienceRequest request) {
        CandidateProfileResponse response = candidateProfileService.updateExperience(currentCandidateId(), experienceId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete experience entry")
    @DeleteMapping("/experience/{experienceId}")
    public ResponseEntity<Void> deleteExperience(@PathVariable UUID experienceId) {
        candidateProfileService.deleteExperience(currentCandidateId(), experienceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add skill entry")
    @PostMapping("/skills")
    public ResponseEntity<CandidateProfileResponse> addSkill(@Valid @RequestBody SkillRequest request) {
        CandidateProfileResponse response = candidateProfileService.addSkill(currentCandidateId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update skill entry")
    @PutMapping("/skills/{skillId}")
    public ResponseEntity<CandidateProfileResponse> updateSkill(@PathVariable UUID skillId,
                                                                @Valid @RequestBody SkillRequest request) {
        CandidateProfileResponse response = candidateProfileService.updateSkill(currentCandidateId(), skillId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete skill entry")
    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(@PathVariable UUID skillId) {
        candidateProfileService.deleteSkill(currentCandidateId(), skillId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add language entry")
    @PostMapping("/languages")
    public ResponseEntity<CandidateProfileResponse> addLanguage(@Valid @RequestBody LanguageRequest request) {
        CandidateProfileResponse response = candidateProfileService.addLanguage(currentCandidateId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update language entry")
    @PutMapping("/languages/{languageId}")
    public ResponseEntity<CandidateProfileResponse> updateLanguage(@PathVariable UUID languageId,
                                                                   @Valid @RequestBody LanguageRequest request) {
        CandidateProfileResponse response = candidateProfileService.updateLanguage(currentCandidateId(), languageId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete language entry")
    @DeleteMapping("/languages/{languageId}")
    public ResponseEntity<Void> deleteLanguage(@PathVariable UUID languageId) {
        candidateProfileService.deleteLanguage(currentCandidateId(), languageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get profile completeness status")
    @GetMapping("/completeness")
    public ResponseEntity<ProfileCompletenessResponse> getCompleteness() {
        return ResponseEntity.ok(candidateProfileService.getCompleteness(currentCandidateId()));
    }

    private UUID currentCandidateId() {
        return candidateIdentityService.requireCurrentCandidateId();
    }
}

