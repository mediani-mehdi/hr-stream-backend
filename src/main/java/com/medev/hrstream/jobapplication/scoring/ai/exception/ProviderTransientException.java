package com.medev.hrstream.jobapplication.scoring.ai.exception;

public class ProviderTransientException extends RuntimeException {
    private final String provider;
    public ProviderTransientException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }
    public String getProvider() { return provider; }
}
