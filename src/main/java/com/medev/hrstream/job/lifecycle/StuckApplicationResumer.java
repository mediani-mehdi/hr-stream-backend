package com.medev.hrstream.job.lifecycle;

import com.medev.hrstream.config.ScoringProperties;
import com.medev.hrstream.jobapplication.JobApplication;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import com.medev.hrstream.jobapplication.scoring.ScoringPipelineOrchestrator;
import com.medev.hrstream.jobapplication.scoring.domain.ScoringContext;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Finds applications stuck in intermediate pipeline states for longer than
 * stuckAfterMinutes (e.g. due to a JVM crash) and re-dispatches them.
 */
@Slf4j
@Component
public class StuckApplicationResumer {

    private static final List<PipelineStatus> STUCK_STATES = List.of(
            PipelineStatus.QUEUED,
            PipelineStatus.EXTRACTING,
            PipelineStatus.RULE_SCORING,
            PipelineStatus.AI_SCORING
    );

    private final JobApplicationRepository applications;
    private final ScoringPipelineOrchestrator orchestrator;
    private final ScoringProperties props;

    public StuckApplicationResumer(JobApplicationRepository applications,
                                   ScoringPipelineOrchestrator orchestrator,
                                   ScoringProperties props) {
        this.applications = applications;
        this.orchestrator = orchestrator;
        this.props = props;
    }

    @Scheduled(cron = "${app.scheduling.stuck-resumer-cron:0 */2 * * * *}")
    @SchedulerLock(name = "stuckApplicationResumer", lockAtLeastFor = "PT10S", lockAtMostFor = "PT4M")
    @Transactional
    public void resumeStuck() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(props.getStuckAfterMinutes());
        List<JobApplication> stuck = applications.findStuckApplications(STUCK_STATES, threshold);

        for (JobApplication app : stuck) {
            log.warn("resumer: requeueing stuck application {} (status={}, lastAttemptAt={})",
                    app.getId(), app.getPipelineStatus(), app.getPipelineLastAttemptAt());

            app.setPipelineStatus(PipelineStatus.QUEUED);
            app.setPipelineLastAttemptAt(LocalDateTime.now());
            applications.save(app);

            ScoringContext ctx = ScoringContext.builder()
                    .applicationId(app.getId())
                    .jobId(app.getJob().getId())
                    .candidateId(app.getCandidate().getId())
                    .cvBlobKey(app.getCvBlobKey())
                    .build();
            orchestrator.processAsync(ctx);
        }
    }
}
