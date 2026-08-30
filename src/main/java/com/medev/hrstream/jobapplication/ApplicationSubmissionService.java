package com.medev.hrstream.jobapplication;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.file.ResumeStorageService;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import com.medev.hrstream.jobapplication.scoring.ScoringPipelineOrchestrator;
import com.medev.hrstream.jobapplication.scoring.domain.ScoringContext;
import com.medev.hrstream.jobapplication.submission.CvFileRejectedException;
import com.medev.hrstream.jobapplication.submission.DuplicateApplicationException;
import com.medev.hrstream.jobapplication.submission.InvalidApplicationTokenException;
import com.medev.hrstream.jobapplication.submission.JobClosedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ApplicationSubmissionService {

    private final JobRepository jobs;
    private final JobApplicationRepository applications;
    private final ResumeStorageService storage;
    private final ScoringPipelineOrchestrator orchestrator;
    private final CandidateRepository candidateRepository;

    @Autowired
    public ApplicationSubmissionService(JobRepository jobs,
                                        JobApplicationRepository applications,
                                        ResumeStorageService storage,
                                        ScoringPipelineOrchestrator orchestrator,
                                        CandidateRepository candidateRepository) {
        this.jobs = jobs;
        this.applications = applications;
        this.storage = storage;
        this.orchestrator = orchestrator;
        this.candidateRepository = candidateRepository;
    }

    /**
     * Submits an application. Persists application + uploads CV in one transaction.
     * Kicks off the pipeline only after the transaction commits.
     */
    @Transactional
    public JobApplication submit(String applicationToken, Candidate candidate, MultipartFile cv) {
        Job job = jobs.findByApplicationToken(applicationToken)
                .orElseGet(() -> jobs.findById(applicationToken)
                        .orElseThrow(() -> new InvalidApplicationTokenException("unknown application token")));

        if (Boolean.TRUE.equals(job.getDeleted()) || job.getStatus() != JobStatus.OPEN) {
            throw new JobClosedException("job is not accepting applications");
        }

        if (applications.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new DuplicateApplicationException("candidate already applied to this job");
        }

        if (cv == null || cv.isEmpty()) {
            throw new CvFileRejectedException("CV file is required");
        }

        ResumeStorageService.StoredObject stored;
        try {
            stored = storage.uploadCandidateCv(candidate.getId(), cv);
        } catch (IllegalArgumentException e) {
            throw new CvFileRejectedException(e.getMessage());
        }

        candidate.setResumeObjectKey(stored.objectKey());
        candidate.setResumeUrl(stored.url());
        candidate.setResumeOriginalName(stored.originalName());
        candidate.setResumeContentType(stored.contentType());
        candidate.setResumeSizeBytes(stored.sizeBytes());
        candidateRepository.save(candidate);

        JobApplication app = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.SUBMITTED)
                .pipelineStatus(PipelineStatus.QUEUED)
                .cvBlobKey(stored.objectKey())
                .pipelineAttemptCount(0)
                .build();
        JobApplication saved = applications.save(app);

        ScoringContext ctx = ScoringContext.builder()
                .applicationId(saved.getId())
                .jobId(job.getId())
                .candidateId(candidate.getId())
                .cvBlobKey(stored.objectKey())
                .build();

        // Ensure pipeline runs AFTER commit — if the tx rolls back, no orphan work.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    orchestrator.processAsync(ctx);
                }
            });
        } else {
            orchestrator.processAsync(ctx);
        }

        return saved;
    }
}
