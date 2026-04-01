package com.medev.hrstream.jobapplication;

import com.medev.hrstream.candidate.CandidateIdentityService;
import com.medev.hrstream.candidate.profile.CandidateProfileService;
import com.medev.hrstream.candidate.profile.dto.ProfileCompletenessResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class JobApplicationController {

    private final JobApplicationService service;
    private final CandidateProfileService candidateProfileService;
    private final CandidateIdentityService candidateIdentityService;
    private final ApplicationScoringService applicationScoringService;

    public JobApplicationController(JobApplicationService service,
                                    CandidateProfileService candidateProfileService,
                                    CandidateIdentityService candidateIdentityService,
                                    ApplicationScoringService applicationScoringService) {
        this.service = service;
        this.candidateProfileService = candidateProfileService;
        this.candidateIdentityService = candidateIdentityService;
        this.applicationScoringService = applicationScoringService;
    }

    @Operation(summary = "Admin - list applications")
    @GetMapping("/applications")
    public ResponseEntity<Page<JobApplication>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(service.findAll(page, size, sortBy, direction));
    }

    @Operation(summary = "Admin - update application status")
    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<JobApplication> updateStatus(
            @PathVariable String id,
            @RequestParam ApplicationStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @Operation(summary = "Admin - get application by id")
    @GetMapping("/applications/{id}")
    public ResponseEntity<JobApplication> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Admin - delete application")
    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Candidate applies to a job by slug")
    @PostMapping("/jobs/{slug}/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<JobApplication> apply(@PathVariable String slug) {
        UUID candidateId = candidateIdentityService.requireCurrentCandidateId();
        ProfileCompletenessResponse completeness = candidateProfileService.getCompleteness(candidateId);
        if (!completeness.isReadyToApply()) {
            throw new IllegalArgumentException("Please complete your profile (experience and skills) before applying");
        }
        var candidate = candidateIdentityService.requireCurrentCandidate();
        JobApplication application = service.applyForSlug(slug, candidate);
        applicationScoringService.scoreApplicationAsync(UUID.fromString(application.getId()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(application);
    }
}
