package com.medev.hrstream.job;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/public/jobs")
@Tag(name = "Public Jobs", description = "Publicly accessible job endpoints (no authentication required)")
public class PublicJobController {

    private final JobRepository jobRepository;

    public PublicJobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Operation(summary = "Get public job details by slug (applicationToken). Returns an applyUrl for candidates.")
    @GetMapping("/{slug}")
    public ResponseEntity<PublicJobResponse> getJob(@PathVariable String slug) {
        Job job = jobRepository.findByApplicationToken(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        PublicJobResponse response = PublicJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .experienceLevel(job.getExperienceLevel())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .skills(job.getSkills())
                .createdAt(job.getCreatedDate())
                .applyUrl("/jobs/" + slug + "/apply")
                .build();

        return ResponseEntity.ok(response);
    }
}
