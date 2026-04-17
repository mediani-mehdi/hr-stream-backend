package com.medev.hrstream.jobapplication.scoring.ai.exception;

public class ProviderMalformedResponseException extends RuntimeException {
    private final String provider;
    public ProviderMalformedResponseException(String provider, String message) {
        super(message);
        this.provider = provider;
    }
    public String getProvider() { return provider; }
}
