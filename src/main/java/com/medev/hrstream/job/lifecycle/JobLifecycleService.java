package com.medev.hrstream.job.lifecycle;

import com.medev.hrstream.config.ScoringProperties;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class JobLifecycleService {

    private final JobRepository jobs;
    private final JobApplicationRepository applications;
    private final ScoringProperties props;

    public JobLifecycleService(JobRepository jobs,
                               JobApplicationRepository applications,
                               ScoringProperties props) {
        this.jobs = jobs;
        this.applications = applications;
        this.props = props;
    }

    @Transactional
    public boolean closeIfCapReached(String jobId) {
        Optional<Job> maybe = jobs.findByIdForUpdate(jobId);
        if (maybe.isEmpty()) return false;
        Job job = maybe.get();
        if (job.getStatus() != JobStatus.OPEN) return false;

        long nonRejected = applications.countByJobIdAndStatusNot(jobId, ApplicationStatus.REJECTED);
        if (nonRejected < props.getMaxApplications()) {
            return false;
        }
        closeJob(job, ClosedReason.CAP_REACHED);
        jobs.save(job);
        log.info("lifecycle: closed job {} (CAP_REACHED, nonRejected={})", jobId, nonRejected);
        return true;
    }

    @Transactional
    public int closeExpiredJobs(LocalDateTime cutoff) {
        List<Job> expired = jobs.findOpenJobsWithDeadlineBefore(cutoff);
        for (Job j : expired) {
            closeJob(j, ClosedReason.DEADLINE_PASSED);
            jobs.save(j);
        }
        if (!expired.isEmpty()) {
            log.info("lifecycle: closed {} jobs past deadline", expired.size());
        }
        return expired.size();
    }

    private void closeJob(Job job, ClosedReason reason) {
        job.setStatus(JobStatus.FILLED);
        job.setClosedAt(LocalDateTime.now());
        job.setClosedReason(reason);
    }
}
