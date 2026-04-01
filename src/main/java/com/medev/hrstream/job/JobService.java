package com.medev.hrstream.job;

import com.medev.hrstream.Gemini.GeminiService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final GeminiService geminiService;

    public JobService(JobRepository jobRepository, GeminiService geminiService) {
        this.jobRepository = jobRepository;
        this.geminiService = geminiService;
    }

    public String save(Job job) {
        job.setStatus(JobStatus.DRAFT);
        jobRepository.save(job);
        return "Job saved successfully";
    }

    public String generate(Job job) {
        job.setStatus(JobStatus.OPEN);
        job.setDescription(geminiService.generateJobDescription(job).getDescription());
        jobRepository.save(job);
        return "Job generated successfully";
    }

    public String publish(String jobId, String status) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.valueOf(status));
        jobRepository.save(job);
        return "Job status is set to "+status+" successfully";
    }

    public String updateStatus(String jobId, JobStatus status) {
        Job job = findById(jobId);
        job.setStatus(status);
        jobRepository.save(job);
        return "Job status updated to " + status + " successfully";
    }

    public Job updateJob(String jobId, Job updatedJob) {
        Job existingJob = findById(jobId);

        existingJob.setTitle(updatedJob.getTitle());
        existingJob.setDescription(updatedJob.getDescription());
        existingJob.setLocation(updatedJob.getLocation());
        existingJob.setExperienceLevel(updatedJob.getExperienceLevel());
        existingJob.setEmploymentType(updatedJob.getEmploymentType());
        existingJob.setCompanyDetails(updatedJob.getCompanyDetails());
        existingJob.setAdditionalInfo(updatedJob.getAdditionalInfo());
        existingJob.setSkills(updatedJob.getSkills());
        existingJob.setStatus(updatedJob.getStatus());

        return jobRepository.save(existingJob);
    }

    public String delete(String jobId) {
        jobRepository.softdeleteById(jobId);
        return "Job deleted successfully";
    }

    public Job findById(String jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public JobResponseDTO generateFromId(String jobId) {
        Job job = findById(jobId);
        return geminiService.generateJobDescription(job);
    }

    public Page<JobResponseDTO> findAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return jobRepository.findAllActive(pageable).map(this::mapToResponseDTO);
    }

    public JobResponseDTO getPublicJobBySlug(String slug) {
        Job job = jobRepository.findByApplicationToken(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        if (Boolean.TRUE.equals(job.getDeleted())) {
            throw new RuntimeException("Job not available");
        }
        return mapToResponseDTO(job);
    }

    private JobResponseDTO mapToResponseDTO(Job job) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .applicationLink(job.getApplyLink())
                .applyUrl(job.getApplyLink())
                .status(job.getStatus())
                .location(job.getLocation())
                .experienceLevel(job.getExperienceLevel())
                .employmentType(job.getEmploymentType())
                .skills(job.getSkills())
                .createdAt(job.getCreatedDate())
                .build();
    }
}
