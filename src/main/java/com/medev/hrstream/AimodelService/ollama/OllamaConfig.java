package com.medev.hrstream.AimodelService.ollama;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    private final OllamaProperties ollamaProperties;

    public OllamaConfig(OllamaProperties ollamaProperties) {
        this.ollamaProperties = ollamaProperties;
    }

    @Bean
    public OpenAiApi ollamaApi() {
        return OpenAiApi.builder()
                .baseUrl(ollamaProperties.getBaseUrl())
                .apiKey(ollamaProperties.getApiKey())
                .build();
    }

    @Bean
    public OpenAiChatModel ollamaChatModel(OpenAiApi ollamaApi) {
        return OpenAiChatModel.builder()
                .openAiApi(ollamaApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(ollamaProperties.getModel())
                        .build())
                .build();
    }

    @Bean
    public String ollamaModelName() {
        return ollamaProperties.getModel();
    }
}
