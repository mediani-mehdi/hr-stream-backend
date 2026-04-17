package com.medev.hrstream.jobapplication.scoring.ai.exception;

public class ProviderRateLimitedException extends RuntimeException {
    private final String provider;
    public ProviderRateLimitedException(String provider, String message) {
        super(message);
        this.provider = provider;
    }
    public String getProvider() { return provider; }
}
