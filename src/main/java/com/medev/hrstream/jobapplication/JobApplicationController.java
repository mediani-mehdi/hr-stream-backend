package com.medev.hrstream.jobapplication;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.candidate.profile.CandidateProfileService;
import com.medev.hrstream.common.ApiError;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class JobApplicationController {

    private final JobApplicationService service;
    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateProfileService profileService;
    private final ApplicationScoringService scoringService;

    public JobApplicationController(
            JobApplicationService service,
            JobApplicationRepository applicationRepository,
            JobRepository jobRepository,
            CandidateRepository candidateRepository,
            CandidateProfileService profileService,
            ApplicationScoringService scoringService) {
        this.service = service;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.profileService = profileService;
        this.scoringService = scoringService;
    }

    @Operation(summary = "Apply to a job (candidate only). Triggers async AI scoring.")
    @PostMapping("/jobs/{slug}/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> apply(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        Job job = jobRepository.findByApplicationToken(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        Candidate candidate = candidateRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));

        if (!profileService.isProfileComplete(candidate.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError(400, "Bad Request",
                            "Profile is incomplete. Please add at least one work experience and one skill before applying.",
                            httpRequest.getRequestURI()));
        }

        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError(409, "Conflict",
                            "You have already applied to this job.",
                            httpRequest.getRequestURI()));
        }

        JobApplication application = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.PENDING)
                .build();
        application = applicationRepository.save(application);

        scoringService.scoreApplicationAsync(application.getId());

        // Return 202 Accepted: application is saved synchronously; AI scoring runs asynchronously.
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(application);
    }

    @GetMapping("/applications")
    public ResponseEntity<Page<JobApplication>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(service.findAll(page, size, sortBy, direction));
    }

    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<JobApplication> updateStatus(
            @PathVariable String id,
            @RequestParam ApplicationStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<JobApplication> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

