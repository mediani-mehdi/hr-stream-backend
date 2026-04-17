package com.medev.hrstream.job.lifecycle;

import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.events.JobApplicationFinalized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for pipeline completions; if the app is non-rejected, runs cap check.
 * Runs after the finalizing transaction commits so the count reflects the latest app.
 */
@Slf4j
@Component
public class JobAutoCloseListener {

    private final JobLifecycleService lifecycle;

    public JobAutoCloseListener(JobLifecycleService lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Async("scoringExecutor")
    @EventListener
    public void onFinalized(JobApplicationFinalized event) {
        if (event.getFinalStatus() == ApplicationStatus.REJECTED) {
            return;
        }
        try {
            lifecycle.closeIfCapReached(event.getJobId());
        } catch (RuntimeException e) {
            log.warn("lifecycle: cap-close failed for job {}: {}", event.getJobId(), e.toString());
        }
    }
}
