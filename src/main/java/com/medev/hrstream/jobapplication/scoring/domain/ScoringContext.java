package com.medev.hrstream.jobapplication.scoring.domain;

import lombok.Builder;
import lombok.Value;

/** Internal DTO passed through the scoring pipeline. */
@Value
@Builder
public class ScoringContext {
    String applicationId;
    String jobId;
    String candidateId;
    String cvBlobKey;
}
