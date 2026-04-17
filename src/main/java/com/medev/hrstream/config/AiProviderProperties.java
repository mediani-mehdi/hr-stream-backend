package com.medev.hrstream.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ai")
@Data
public class AiProviderProperties {

    private Providers providers = new Providers();
    private Scoring scoring = new Scoring();

    @Data
    public static class Providers {
        /** Ordered list of provider names, tried in order. */
        private List<String> order = List.of("openrouter", "gemini", "claude", "glm");

        private ProviderConfig openrouter = new ProviderConfig();
        private ProviderConfig gemini = new ProviderConfig();
        private ProviderConfig claude = new ProviderConfig();
        private ProviderConfig glm = new ProviderConfig();
    }

    @Data
    public static class ProviderConfig {
        private boolean enabled = false;
        private String apiKey;
        private String baseUrl;
        private String model;
        private long timeoutMs = 30_000;
    }

    @Data
    public static class Scoring {
        private int maxRetriesPerProvider = 1;
        private long backoffMs = 500;
    }
}
