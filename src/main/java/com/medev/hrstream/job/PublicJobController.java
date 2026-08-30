package com.medev.hrstream.job;

import com.medev.hrstream.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicJobController {

    private final JobService jobService;

    public PublicJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "List all public jobs")
    @GetMapping({"/public/jobs", "/public/jobs/"})
    public ResponseEntity<PageResponse<JobResponseDTO>> getAllPublicJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<JobResponseDTO> result = jobService.findAllPublicJobs(pageable);
        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        ));
    }

    @Operation(summary = "Public job details by slug")
    @GetMapping({"/public/jobs/{slug}", "/public/jobs/{slug}/"})
    public ResponseEntity<JobResponseDTO> getJob(@PathVariable String slug) {
        return ResponseEntity.ok(jobService.getPublicJobBySlug(slug));
    }
}

