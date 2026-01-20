package com.medev.hrstream.job;

import com.medev.hrstream.Gemini.GeminiService;
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

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> findById(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.findById(jobId));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> delete(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.delete(jobId));
    }

    @GetMapping("/generate-description/{jobTitle}")
    public ResponseEntity<String> generateJobDescription(@PathVariable String jobTitle) {
        String description = geminiService.generateJobDescription(jobTitle);
        return ResponseEntity.ok(description);
    }


}
