package com.medev.hrstream.jobapplication.scoring.extractor;

import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;

public class ExtractionFailedException extends RuntimeException {

    private final ProcessingErrorCode code;

    public ExtractionFailedException(ProcessingErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ExtractionFailedException(ProcessingErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ProcessingErrorCode getCode() {
        return code;
    }
}
