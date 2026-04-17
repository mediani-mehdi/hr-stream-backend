package com.medev.hrstream.jobapplication.scoring.ai.exception;

public class ProviderQuotaExceededException extends RuntimeException {
    private final String provider;
    public ProviderQuotaExceededException(String provider, String message) {
        super(message);
        this.provider = provider;
    }
    public String getProvider() { return provider; }
}
