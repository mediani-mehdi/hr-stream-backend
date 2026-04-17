package com.medev.hrstream.jobapplication.scoring.ai.exception;

import java.util.List;

public class AllProvidersExhaustedException extends RuntimeException {
    private final List<String> attempted;
    public AllProvidersExhaustedException(List<String> attempted, Throwable lastCause) {
        super("all AI providers failed: " + attempted, lastCause);
        this.attempted = attempted;
    }
    public List<String> getAttempted() { return attempted; }
}
