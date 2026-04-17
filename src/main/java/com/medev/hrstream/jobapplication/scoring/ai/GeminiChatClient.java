package com.medev.hrstream.jobapplication.scoring.ai;

import com.medev.hrstream.Gemini.GeminiService;
import com.medev.hrstream.config.AiProviderProperties;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderRateLimitedException;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderTransientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeminiChatClient implements AiChatClient {

    public static final String NAME = "gemini";

    private final GeminiService gemini;
    private final AiProviderProperties.ProviderConfig cfg;

    @Autowired
    public GeminiChatClient(GeminiService gemini, AiProviderProperties props) {
        this.gemini = gemini;
        this.cfg = props.getProviders().getGemini();
    }

    @Override
    public String name() { return NAME; }

    @Override
    public boolean isAvailable() {
        return cfg.isEnabled() && cfg.getApiKey() != null && !cfg.getApiKey().isBlank();
    }

    @Override
    @CircuitBreaker(name = "ai-gemini")
    public AiChatResult complete(String prompt) {
        try {
            String raw = gemini.generateJsonResponse(prompt);
            return AiChatResult.builder().providerName(NAME).rawResponse(raw).build();
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("rate") || msg.contains("429")) {
                throw new ProviderRateLimitedException(NAME, e.getMessage());
            }
            throw new ProviderTransientException(NAME, "gemini call failed", e);
        }
    }
}
