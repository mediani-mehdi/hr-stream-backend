package com.medev.hrstream.jobapplication.scoring.aiscorer;

import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;

public class AiScoringFailedException extends RuntimeException {
    private final ProcessingErrorCode code;

    public AiScoringFailedException(ProcessingErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public AiScoringFailedException(ProcessingErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ProcessingErrorCode getCode() { return code; }
}
