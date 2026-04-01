package com.medev.hrstream.job;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicJobController {

    private final JobService jobService;

    public PublicJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "Public job details by slug")
    @GetMapping("/public/jobs/{slug}")
    public ResponseEntity<JobResponseDTO> getJob(@PathVariable String slug) {
        return ResponseEntity.ok(jobService.getPublicJobBySlug(slug));
    }
}

