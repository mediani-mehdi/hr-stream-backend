package com.medev.hrstream.AimodelService;

import com.medev.hrstream.AimodelService.lmstudio.LMStudioService;
import com.medev.hrstream.AimodelService.ollama.OllamaService;
import com.medev.hrstream.AimodelService.openaicompatible.OpenAICompatibleService;
import com.medev.hrstream.Gemini.GeminiService;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobResponseDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Unified AI Model Service that routes requests to the appropriate AI provider
 * based on the model name or provider type.
 */
@Service
public class AIModelService {

    private final Map<String, Function<Job, JobResponseDTO>> modelToServiceMap;
    private final Map<String, Function<String, String>> modelToGenerateMap;

    public AIModelService(OllamaService ollamaService,
                          LMStudioService lmStudioService,
                          OpenAICompatibleService openAICompatibleService,
                          GeminiService geminiService) {
        this.modelToServiceMap = new HashMap<>();
        this.modelToGenerateMap = new HashMap<>();
        
        // Register Ollama models
        registerModel("ollama", ollamaService::generateJobDescription);
        registerModel("llama", ollamaService::generateJobDescription);
        registerModel("llama3", ollamaService::generateJobDescription);
        registerModel("llama3.2", ollamaService::generateJobDescription);
        
        // Register LM Studio models
        registerModel("lmstudio", lmStudioService::generateJobDescription);
        registerModel("lm-studio", lmStudioService::generateJobDescription);
        registerModel("meta-llama", lmStudioService::generateJobDescription);
        
        // Register OpenAI Compatible models
        registerModel("openai", openAICompatibleService::generateJobDescription);
        registerModel("openai-compatible", openAICompatibleService::generateJobDescription);
        registerModel("gpt", openAICompatibleService::generateJobDescription);
        registerModel("gpt-4", openAICompatibleService::generateJobDescription);
        registerModel("gpt-3.5", openAICompatibleService::generateJobDescription);
        
        // Register Gemini models
        registerModel("gemini", geminiService::generateJobDescription);
        registerModel("gemini-1.5", geminiService::generateJobDescription);
        registerModel("gemini-2.0", geminiService::generateJobDescription);
        registerModel("gemini-flash", geminiService::generateJobDescription);
        
        // Register text generation mappings
        registerTextModel("ollama", ollamaService::generateDescription);
        registerTextModel("llama", ollamaService::generateDescription);
        registerTextModel("lmstudio", lmStudioService::generateDescription);
        registerTextModel("lm-studio", lmStudioService::generateDescription);
        registerTextModel("openai", openAICompatibleService::generateDescription);
        registerTextModel("openai-compatible", openAICompatibleService::generateDescription);
        registerTextModel("gpt", openAICompatibleService::generateDescription);
        registerTextModel("gemini", geminiService::generateJsonResponse);
    }

    /**
     * Register a model name with its corresponding service
     */
    private void registerModel(String modelName, Function<Job, JobResponseDTO> service) {
        modelToServiceMap.put(modelName.toLowerCase(), service);
    }

    private void registerTextModel(String modelName, Function<String, String> service) {
        modelToGenerateMap.put(modelName.toLowerCase(), service);
    }

    /**
     * Generate a job description using the specified AI model
     * @param modelName The name of the AI model to use (e.g., "ollama", "lmstudio", "openai", "gemini")
     * @param job The job to generate a description for
     * @return JobResponseDTO with the generated description
     * @throws IllegalArgumentException if the model name is not supported
     */
    public JobResponseDTO generateJobDescription(String modelName, Job job) {
        Function<Job, JobResponseDTO> service = modelToServiceMap.get(modelName.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported AI model: " + modelName + 
                ". Supported models: " + String.join(", ", modelToServiceMap.keySet()));
        }
        return service.apply(job);
    }

    /**
     * Generate a text response using the specified AI model
     * @param modelName The name of the AI model to use
     * @param prompt The prompt to send to the AI
     * @return The generated text response
     * @throws IllegalArgumentException if the model name is not supported
     */
    public String generateDescription(String modelName, String prompt) {
        Function<String, String> service = modelToGenerateMap.get(modelName.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported AI model: " + modelName + 
                ". Supported models: " + String.join(", ", modelToGenerateMap.keySet()));
        }
        return service.apply(prompt);
    }

    /**
     * Get the list of supported AI model names
     */
    public java.util.List<String> getSupportedModels() {
        return java.util.List.copyOf(modelToServiceMap.keySet());
    }

    /**
     * Check if a model name is supported
     */
    public boolean isModelSupported(String modelName) {
        return modelToServiceMap.containsKey(modelName.toLowerCase());
    }
}
