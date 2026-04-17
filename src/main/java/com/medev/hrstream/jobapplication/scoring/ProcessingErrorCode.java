package com.medev.hrstream.jobapplication.scoring;

public enum ProcessingErrorCode {
    CV_EMPTY,
    CV_PASSWORD_PROTECTED,
    CV_CORRUPTED,
    CV_IMAGE_ONLY,
    RULE_SCORER_ERROR,
    AI_PROVIDERS_EXHAUSTED,
    AI_RESPONSE_MALFORMED,
    UNEXPECTED
}
