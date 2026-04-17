package com.medev.hrstream.jobapplication.events;

import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import lombok.Builder;
import lombok.Value;

/**
 * Published via ApplicationEventPublisher when the pipeline reaches DONE or FAILED.
 * Consumers: JobAutoCloseListener (Phase 6).
 */
@Value
@Builder
public class JobApplicationFinalized {
    String applicationId;
    String jobId;
    String candidateId;
    ApplicationStatus finalStatus;
    PipelineStatus pipelineStatus;
    Integer aiScore;
    Integer ruleScore;
}
