package com.medev.hrstream.job.lifecycle;

import com.medev.hrstream.config.ScoringProperties;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobLifecycleServiceTest {

    private final JobRepository jobs = mock(JobRepository.class);
    private final JobApplicationRepository applications = mock(JobApplicationRepository.class);
    private final ScoringProperties props = new ScoringProperties();

    private final JobLifecycleService service = new JobLifecycleService(jobs, applications, props);

    @Test
    void closesJobWhenCapReached() {
        props.setMaxApplications(3);
        Job job = openJob();
        when(jobs.findByIdForUpdate("j1")).thenReturn(Optional.of(job));
        when(applications.countByJobIdAndStatusNot("j1", ApplicationStatus.REJECTED)).thenReturn(3L);

        boolean closed = service.closeIfCapReached("j1");

        assertThat(closed).isTrue();
        assertThat(job.getStatus()).isEqualTo(JobStatus.FILLED);
        assertThat(job.getClosedReason()).isEqualTo(ClosedReason.CAP_REACHED);
        assertThat(job.getClosedAt()).isNotNull();
        verify(jobs).save(job);
    }

    @Test
    void doesNotCloseBelowCap() {
        props.setMaxApplications(3);
        Job job = openJob();
        when(jobs.findByIdForUpdate("j1")).thenReturn(Optional.of(job));
        when(applications.countByJobIdAndStatusNot("j1", ApplicationStatus.REJECTED)).thenReturn(2L);

        boolean closed = service.closeIfCapReached("j1");

        assertThat(closed).isFalse();
        assertThat(job.getStatus()).isEqualTo(JobStatus.OPEN);
        verify(jobs, never()).save(any());
    }

    @Test
    void skipsAlreadyClosedJob() {
        props.setMaxApplications(3);
        Job job = openJob();
        job.setStatus(JobStatus.FILLED);
        when(jobs.findByIdForUpdate("j1")).thenReturn(Optional.of(job));

        boolean closed = service.closeIfCapReached("j1");

        assertThat(closed).isFalse();
        verify(jobs, never()).save(any());
    }

    @Test
    void closeExpiredJobsUpdatesStatusAndReason() {
        Job j1 = openJob(); j1.setId("j1");
        Job j2 = openJob(); j2.setId("j2");
        when(jobs.findOpenJobsWithDeadlineBefore(any()))
                .thenReturn(java.util.List.of(j1, j2));

        int closedCount = service.closeExpiredJobs(LocalDateTime.now());

        assertThat(closedCount).isEqualTo(2);
        assertThat(j1.getStatus()).isEqualTo(JobStatus.FILLED);
        assertThat(j1.getClosedReason()).isEqualTo(ClosedReason.DEADLINE_PASSED);
        assertThat(j2.getStatus()).isEqualTo(JobStatus.FILLED);
        verify(jobs, times(2)).save(any(Job.class));
    }

    private Job openJob() {
        Job j = Job.builder()
                .id("j1")
                .title("Role")
                .status(JobStatus.OPEN)
                .deleted(false)
                .build();
        return j;
    }
}
