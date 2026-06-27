package com.medev.hrstream.AimodelService.ollama;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    private final OllamaProperties ollamaProperties;

    public OllamaConfig(OllamaProperties ollamaProperties) {
        this.ollamaProperties = ollamaProperties;
    }

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi(ollamaProperties.getBaseUrl());
    }

    @Bean
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
        return new OllamaChatModel(ollamaApi, ollamaProperties.getModel());
    }

    @Bean
    public String ollamaModelName() {
        return ollamaProperties.getModel();
    }
}
