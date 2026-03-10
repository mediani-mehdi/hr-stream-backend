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
        // Manually parse JSON to avoid "Content-Type 'application/octet-stream' is not supported"
        // if the client doesn't set content-type for the candidate part correctly.
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Candidate candidate = mapper.readValue(candidateJson, Candidate.class);
        return ResponseEntity.ok(candidateService.applyWithResume(token, candidate, resume));
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

    @GetMapping("/{id}/resume")
    public ResponseEntity<String> getResume(@PathVariable String id) {
        // Returns the URL to view the resume. For direct streaming, we would return ByteArrayResource.
        // Assuming client redirects or displays the URL.
        return ResponseEntity.ok(candidateService.getResumeUrl(id));
    }
}
