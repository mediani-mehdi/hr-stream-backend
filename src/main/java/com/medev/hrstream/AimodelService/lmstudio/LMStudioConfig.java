package com.medev.hrstream.AimodelService.lmstudio;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LMStudioProperties.class)
public class LMStudioConfig {

    private final LMStudioProperties lmStudioProperties;

    public LMStudioConfig(LMStudioProperties lmStudioProperties) {
        this.lmStudioProperties = lmStudioProperties;
    }

    @Bean
    public OpenAiApi lmStudioApi() {
        return OpenAiApi.builder()
                .baseUrl(lmStudioProperties.getBaseUrl())
                .apiKey(lmStudioProperties.getApiKey())
                .build();
    }

    @Bean
    public OpenAiChatModel lmStudioChatModel(OpenAiApi lmStudioApi) {
        return OpenAiChatModel.builder()
                .openAiApi(lmStudioApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(lmStudioProperties.getModel())
                        .build())
                .build();
    }

    @Bean
    public String lmStudioModelName() {
        return lmStudioProperties.getModel();
    }
}
