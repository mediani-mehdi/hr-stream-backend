package com.medev.hrstream.candidate;

import com.medev.hrstream.jobapplication.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    public ResponseEntity<Candidate> save(@RequestBody Candidate candidate) {
        return ResponseEntity.ok(candidateService.save(candidate));
    }

    @PostMapping("/apply/{token}")
    public ResponseEntity<JobApplication> apply(@PathVariable String token, @RequestBody Candidate candidate) {
        return ResponseEntity.ok(candidateService.apply(token, candidate));
    }

    @PostMapping(value = "/apply/{token}/with-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateApplyResponse> applyWithResume(
            @PathVariable String token,
            @RequestPart("candidate") String candidateJson,
            @RequestPart("resume") MultipartFile resume
    ) throws com.fasterxml.jackson.core.JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Candidate candidate = mapper.readValue(candidateJson, Candidate.class);
        return ResponseEntity.ok(candidateService.applyWithResume(token, candidate, resume));
    }

    @GetMapping("/me")
    public ResponseEntity<Candidate> getMe(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(candidateService.findByEmail(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<Candidate> updateMe(java.security.Principal principal, @RequestBody Candidate candidate) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        Candidate me = candidateService.findByEmail(principal.getName());
        return ResponseEntity.ok(candidateService.update(me.getId(), candidate));
    }

    @PostMapping(value = "/me/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvResponse> uploadMyCv(
            java.security.Principal principal,
            @RequestPart("file") MultipartFile file
    ) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        Candidate me = candidateService.findByEmail(principal.getName());
        CvResponse response = candidateService.uploadCv(me.getId(), file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/cv")
    public ResponseEntity<CvResponse> getMyCv(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        Candidate me = candidateService.findByEmail(principal.getName());
        CvResponse response = candidateService.getCv(me.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<Candidate>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(candidateService.findAll(page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidate> findById(@PathVariable String id) {
        return ResponseEntity.ok(candidateService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Candidate> update(@PathVariable String id, @RequestBody Candidate candidate) {
        return ResponseEntity.ok(candidateService.update(id, candidate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── CV Endpoints: /api/candidates/{candidateId}/cv ───────────────

    /**
     * Upload a CV for a candidate.
     * Accepts multipart file (PDF or Word only, max 10MB).
     * Stores in MinIO under key: cvs/{candidateId}/{UUID}.{ext}
     * Saves reference in PostgreSQL.
     */
    @PostMapping(value = "/{candidateId}/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvResponse> uploadCv(
            @PathVariable String candidateId,
            @RequestPart("file") MultipartFile file
    ) {
        CvResponse response = candidateService.uploadCv(candidateId, file);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the active CV for a candidate.
     * Returns a presigned URL valid for 1 hour.
     */
    @GetMapping("/{candidateId}/cv")
    public ResponseEntity<CvResponse> getCv(@PathVariable String candidateId) {
        CvResponse response = candidateService.getCv(candidateId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete the CV for a candidate.
     * Deletes the file from MinIO and clears the record in PostgreSQL.
     */
    @DeleteMapping("/{candidateId}/cv")
    public ResponseEntity<Void> deleteCv(@PathVariable String candidateId) {
        candidateService.deleteCv(candidateId);
        return ResponseEntity.noContent().build();
    }

    // ── Legacy endpoint (kept for backward compatibility) ────────────

    @GetMapping("/{id}/resume")
    public ResponseEntity<String> getResume(@PathVariable String id) {
        return ResponseEntity.ok(candidateService.getResumeUrl(id));
    }
}
