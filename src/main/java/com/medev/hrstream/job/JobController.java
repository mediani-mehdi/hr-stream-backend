package com.medev.hrstream.job;

import com.medev.hrstream.Gemini.GeminiService;
import com.medev.hrstream.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final GeminiService geminiService;
    public JobController(JobService jobService, GeminiService geminiService) {
        this.jobService = jobService;
        this.geminiService = geminiService;
    }


    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Job job) {
        return  ResponseEntity.ok(jobService.save(job));
    }

    @PostMapping("/{jobId}/{status}")
    public ResponseEntity<String> publish(@PathVariable String jobId, @PathVariable String status) {
        return ResponseEntity.ok(jobService.publish(jobId, status));
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable String jobId, @RequestParam JobStatus status) {
        return ResponseEntity.ok(jobService.updateStatus(jobId, status));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<Job> updateJob(@PathVariable String jobId, @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(jobId, job));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> findById(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.findById(jobId));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> delete(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.delete(jobId));
    }

    @PostMapping("/generate-description")
    public ResponseEntity<String> generateJobDescription(@RequestBody Job job) {
        String description = geminiService.generateJobDescription(job).getDescription();
        return ResponseEntity.ok(description);
    }

    @PostMapping("/{jobId}/generate")
    public ResponseEntity<JobResponseDTO> generateFromId(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.generateFromId(jobId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<JobResponseDTO> result = jobService.findAll(page, size, sortBy, direction);
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
}
