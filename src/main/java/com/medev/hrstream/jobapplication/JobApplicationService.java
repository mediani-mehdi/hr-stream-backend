package com.medev.hrstream.jobapplication;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;

    public JobApplicationService(JobApplicationRepository repository, JobRepository jobRepository, CandidateRepository candidateRepository) {
        this.repository = repository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
    }

    public JobApplication apply(String token, Candidate candidateData) {
        Job job = jobRepository.findByApplicationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid application token or job not found"));

        Candidate candidate = candidateRepository.findByEmail(candidateData.getEmail())
                .map(existing -> {
                    existing.setFirstName(candidateData.getFirstName());
                    existing.setLastName(candidateData.getLastName());
                    existing.setPhone(candidateData.getPhone());
                    existing.setNiveauEtude(candidateData.getNiveauEtude());
                    existing.setDomaineExpertise(candidateData.getDomaineExpertise());
                    existing.setExperienceProfessionnelle(candidateData.getExperienceProfessionnelle());
                    return candidateRepository.save(existing);
                })
                .orElseGet(() -> candidateRepository.save(candidateData));

        JobApplication application = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.PENDING)
                .build();

        return repository.save(application);
    }
// ...existing code...
    public JobApplication updateStatus(String id, ApplicationStatus status) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        return repository.save(application);
    }

    public Page<JobApplication> findAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(pageable);
    }

    public JobApplication findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}

