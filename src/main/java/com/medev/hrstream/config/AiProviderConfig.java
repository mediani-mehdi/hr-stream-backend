package com.medev.hrstream.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiProviderConfig {

    @Bean
    @Qualifier("openrouterChatModel")
    @ConditionalOnProperty(prefix = "ai.providers.openrouter", name = "enabled", havingValue = "true")
    public OpenAiChatModel openrouterChatModel(AiProviderProperties props) {
        AiProviderProperties.ProviderConfig cfg = props.getProviders().getOpenrouter();
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(cfg.getBaseUrl())
                .apiKey(cfg.getApiKey())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(cfg.getModel())
                        .build())
                .build();
    }

    @Bean
    @Qualifier("glmChatModel")
    @ConditionalOnProperty(prefix = "ai.providers.glm", name = "enabled", havingValue = "true")
    public OpenAiChatModel glmChatModel(AiProviderProperties props) {
        AiProviderProperties.ProviderConfig cfg = props.getProviders().getGlm();
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(cfg.getBaseUrl())
                .apiKey(cfg.getApiKey())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(cfg.getModel())
                        .build())
                .build();
    }

    @Bean
    @Qualifier("claudeChatModel")
    @ConditionalOnProperty(prefix = "ai.providers.claude", name = "enabled", havingValue = "true")
    public AnthropicChatModel claudeChatModel(AiProviderProperties props) {
        AiProviderProperties.ProviderConfig cfg = props.getProviders().getClaude();
        AnthropicApi api = AnthropicApi.builder()
                .baseUrl(cfg.getBaseUrl())
                .apiKey(cfg.getApiKey())
                .build();
        return AnthropicChatModel.builder()
                .anthropicApi(api)
                .defaultOptions(org.springframework.ai.anthropic.AnthropicChatOptions.builder()
                        .model(cfg.getModel())
                        .build())
                .build();
    }

    @Bean
    @Qualifier("scoringLmStudioChatModel")
    @ConditionalOnProperty(prefix = "ai.providers.lm-studio", name = "enabled", havingValue = "true")
    public OpenAiChatModel scoringLmStudioChatModel(AiProviderProperties props) {
        AiProviderProperties.ProviderConfig cfg = props.getProviders().getLmStudio();
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(cfg.getBaseUrl())
                .apiKey(cfg.getApiKey())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(cfg.getModel())
                        .build())
                .build();
    }

    @Bean
    @Qualifier("openAICompatibleChatModel")
    @ConditionalOnProperty(prefix = "ai.providers.openai-compatible", name = "enabled", havingValue = "true")
    public OpenAiChatModel openAICompatibleChatModel(AiProviderProperties props) {
        AiProviderProperties.ProviderConfig cfg = props.getProviders().getOpenaiCompatible();
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(cfg.getBaseUrl())
                .apiKey(cfg.getApiKey())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(cfg.getModel())
                        .build())
                .build();
    }
}
