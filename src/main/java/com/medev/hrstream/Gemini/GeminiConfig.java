package com.medev.hrstream.Gemini;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        String cleanKey = apiKey != null ? apiKey.replace("\"", "").trim() : null;
        return new Client();
    }

    @Bean
    public String geminiModelName(@Value("${gemini.model:gemini-2.0-flash}") String modelName) {
        return modelName.replace("\"", "").trim();
    }
}

