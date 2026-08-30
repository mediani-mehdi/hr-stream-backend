package com.medev.hrstream.job;

import com.medev.hrstream.AimodelService.AIModelService;
import com.medev.hrstream.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final AIModelService aiModelService;
    public JobController(JobService jobService, AIModelService aiModelService) {
        this.jobService = jobService;
        this.aiModelService = aiModelService;
    }


    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Job job) {
        return  ResponseEntity.ok(jobService.save(job));
    }

    @PostMapping("/{jobId}/publish/{status}")
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
        JobResponseDTO response = aiModelService.generateJobDescription("lmstudio", job);
        return ResponseEntity.ok(response.getDescription());
    }

    @PostMapping("/{jobId}/generate")
    public ResponseEntity<JobResponseDTO> generateFromId(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.generateFromId(jobId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobResponseDTO>> findAll(
            @RequestParam(required = false) List<JobStatus> status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) com.medev.hrstream.job.lifecycle.ClosedReason closedReason,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<JobResponseDTO> result = jobService.findAll(status, department, location, closedReason, search, page, size, sortBy, direction);
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

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(jobService.findUniqueDepartments());
    }

    @GetMapping("/locations")
    public ResponseEntity<List<String>> getLocations() {
        return ResponseEntity.ok(jobService.findUniqueLocations());
    }
}
