package com.medev.hrstream.jobapplication.scoring.ai;

import com.medev.hrstream.config.AiProviderProperties;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderRateLimitedException;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderTransientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OpenRouterChatClient implements AiChatClient {

    public static final String NAME = "openrouter";

    private final ObjectProvider<OpenAiChatModel> modelProvider;
    private final AiProviderProperties.ProviderConfig cfg;

    public OpenRouterChatClient(
            @Qualifier("openrouterChatModel") ObjectProvider<OpenAiChatModel> modelProvider,
            AiProviderProperties props) {
        this.modelProvider = modelProvider;
        this.cfg = props.getProviders().getOpenrouter();
    }

    @Override
    public String name() { return NAME; }

    @Override
    public boolean isAvailable() {
        return cfg.isEnabled()
                && cfg.getApiKey() != null && !cfg.getApiKey().isBlank()
                && modelProvider.getIfAvailable() != null;
    }

    @Override
    @CircuitBreaker(name = "ai-openrouter")
    public AiChatResult complete(String prompt) {
        OpenAiChatModel model = modelProvider.getIfAvailable();
        if (model == null) {
            throw new ProviderTransientException(NAME, "no OpenRouter chat model configured", null);
        }
        try {
            ChatResponse resp = model.call(new Prompt(prompt));
            String text = resp.getResult().getOutput().getText();
            return AiChatResult.builder().providerName(NAME).rawResponse(text).build();
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("rate") || msg.contains("429")) {
                throw new ProviderRateLimitedException(NAME, e.getMessage());
            }
            throw new ProviderTransientException(NAME, "openrouter call failed", e);
        }
    }
}
