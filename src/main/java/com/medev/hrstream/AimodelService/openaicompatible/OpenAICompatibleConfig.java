package com.medev.hrstream.AimodelService.openaicompatible;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAICompatibleConfig {

    private final OpenAICompatibleProperties openAICompatibleProperties;

    public OpenAICompatibleConfig(OpenAICompatibleProperties openAICompatibleProperties) {
        this.openAICompatibleProperties = openAICompatibleProperties;
    }

    @Bean
    public OpenAiApi openAICompatibleApi() {
        return OpenAiApi.builder()
                .baseUrl(openAICompatibleProperties.getBaseUrl())
                .apiKey(openAICompatibleProperties.getApiKey())
                .build();
    }

    @Bean
    public OpenAiChatModel openAICompatibleChatModel(OpenAiApi openAICompatibleApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAICompatibleApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(openAICompatibleProperties.getModel())
                        .build())
                .build();
    }

    @Bean
    public String openAICompatibleModelName() {
        return openAICompatibleProperties.getModel();
    }
}
