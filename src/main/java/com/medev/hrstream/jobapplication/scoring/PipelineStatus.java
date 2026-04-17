package com.medev.hrstream.jobapplication.scoring;

public enum PipelineStatus {
    QUEUED,
    EXTRACTING,
    RULE_SCORING,
    AI_SCORING,
    DONE,
    FAILED
}
