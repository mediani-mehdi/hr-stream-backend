package com.medev.hrstream.AimodelService.openaicompatible;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openai-compatible")
public class OpenAICompatibleProperties {
    private String baseUrl = "http://localhost:8000";
    private String apiKey;
    private String model = "gpt-4";
    private long timeoutMs = 30000;
}
