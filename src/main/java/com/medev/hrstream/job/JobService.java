package com.medev.hrstream.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public String save(Job job) {
        job.setStatus(JobStatus.DRAFT);
        jobRepository.save(job);
        return "Job saved successfully";
    }

    public String publish(String jobId, String status) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.valueOf(status));
        jobRepository.save(job);
        return "Job status is set to "+status+" successfully";
    }

    public String delete(String jobId) {
        jobRepository.softdeleteById(jobId);
        return "Job deleted successfully";
    }

    public Job findById(String jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
    }

//    public Page<Job> findAll(int page, int size, List<JobStatus> statuses) {
//        Pageable pageable = PageRequest.of(page, size);
//        return jobRepository.findAll(pageable, statuses);
//    }

}
