package com.medev.hrstream.jobapplication;

import com.medev.hrstream.candidate.CandidateIdentityService;
import com.medev.hrstream.candidate.profile.CandidateProfileService;
import com.medev.hrstream.candidate.profile.dto.ProfileCompletenessResponse;
import com.medev.hrstream.jobapplication.dto.ApplicationDetailDTO;
import com.medev.hrstream.jobapplication.submission.ProfileIncompleteException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class JobApplicationController {

    private final JobApplicationService service;
    private final ApplicationSubmissionService submissionService;
    private final CandidateProfileService candidateProfileService;
    private final CandidateIdentityService candidateIdentityService;
    private final HrPanelApplicationService hrPanelService;

    public JobApplicationController(JobApplicationService service,
                                    ApplicationSubmissionService submissionService,
                                    CandidateProfileService candidateProfileService,
                                    CandidateIdentityService candidateIdentityService,
                                    HrPanelApplicationService hrPanelService) {
        this.service = service;
        this.submissionService = submissionService;
        this.candidateProfileService = candidateProfileService;
        this.candidateIdentityService = candidateIdentityService;
        this.hrPanelService = hrPanelService;
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

    @Operation(summary = "Candidate applies to a job by slug with a CV upload")
    @PostMapping(value = "/jobs/{slug}/apply", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<JobApplication> apply(
            @PathVariable String slug,
            @RequestPart("cv") MultipartFile cv) {
        UUID candidateId = candidateIdentityService.requireCurrentCandidateId();
        ProfileCompletenessResponse completeness = candidateProfileService.getCompleteness(candidateId);
        if (!completeness.isReadyToApply()) {
            if (!completeness.isHasBasicInfo()) {
                throw new ProfileIncompleteException("Please complete your profile basic information (first name and last name) before applying.");
            }
            if (!completeness.isHasExperience()) {
                throw new ProfileIncompleteException("Please add at least one professional experience entry to your profile before applying.");
            }
            if (!completeness.isHasEducation()) {
                throw new ProfileIncompleteException("Please add at least one education or certification entry to your profile before applying.");
            }
            if (!completeness.isHasSkills()) {
                throw new ProfileIncompleteException("Please add at least one skill to your profile before applying.");
            }
            throw new ProfileIncompleteException("Please complete your candidate profile details before applying.");
        }
        var candidate = candidateIdentityService.requireCurrentCandidate();
        JobApplication application = submissionService.submit(slug, candidate, cv);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(application);
    }

    @Operation(summary = "Candidate - list own applications")
    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<java.util.List<JobApplication>> getMyApplications() {
        UUID candidateId = candidateIdentityService.requireCurrentCandidateId();
        java.util.List<JobApplication> applications = service.findByCandidateId(candidateId.toString());
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Admin - get applications by job id")
    @GetMapping("/applications/job/{jobId}")
    public ResponseEntity<java.util.List<JobApplication>> getApplicationsByJobId(@PathVariable String jobId) {
        return ResponseEntity.ok(service.findByJobId(jobId));
    }

    // ========== HR PANEL ENDPOINTS ==========
    
    @Operation(summary = "HR Panel - get detailed application with scoring data")
    @GetMapping("/hr/applications/{id}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationDetailDTO> getApplicationDetail(@PathVariable String id) {
        return ResponseEntity.ok(hrPanelService.getApplicationDetail(id));
    }

    @Operation(summary = "HR Panel - get all applications for a job with scores, paginated")
    @GetMapping("/hr/applications/job/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ApplicationDetailDTO>> getJobApplicationsWithScores(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationDetailDTO> applications = hrPanelService.getApplicationsForJob(jobId, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "HR Panel - get applications for a job ranked by overall score")
    @GetMapping("/hr/applications/job/{jobId}/ranked")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<ApplicationDetailDTO>> getJobApplicationsRankedByScore(@PathVariable String jobId) {
        return ResponseEntity.ok(hrPanelService.getApplicationsForJobSortedByScore(jobId));
    }

    @Operation(summary = "HR Panel - get dashboard statistics for a job")
    @GetMapping("/hr/jobs/{jobId}/applications-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobApplicationDashboardDTO> getJobApplicationsDashboard(@PathVariable String jobId) {
        return ResponseEntity.ok(hrPanelService.getJobDashboard(jobId));
    }

    @Operation(summary = "HR Panel - get applications by status with scoring details")
    @GetMapping("/hr/applications/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<ApplicationDetailDTO>> getApplicationsByStatus(@PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(hrPanelService.getApplicationsByStatus(status));
    }

    @Operation(summary = "HR Panel - get applications by candidate id")
    @GetMapping("/hr/applications/candidate/{candidateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<ApplicationDetailDTO>> getCandidateApplications(@PathVariable String candidateId) {
        return ResponseEntity.ok(hrPanelService.getApplicationsForCandidate(candidateId));
    }
}
