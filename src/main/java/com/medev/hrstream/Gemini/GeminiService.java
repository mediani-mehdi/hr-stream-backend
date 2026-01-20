package com.medev.hrstream.Gemini;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;
    private final String modelName;

    public GeminiService(@Value("${spring.ai.google.genai.api-key}") String apiKey,
                         @Value("${spring.ai.google.genai.chat.options.model}") String modelName) {
        // FIX 1: Use the builder to explicitly set the API Key from properties.
        // 'new Client()' would ignore the apiKey variable and look for an Env Var instead.
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
        this.modelName = modelName;
    }

    public String generateJobDescription(String jobTitle) {
        String prompt = String.format(
                "Act as an Expert HR Recruiter. Write a professional, engaging Job Description for the role of '%s'.\n\n" +
                        "Structure the response in clean Markdown:\n" +
                        "## Role Overview\n" +
                        "## Key Responsibilities (Bullet points)\n" +
                        "## Required Skills (Bullet points)\n" +
                        "## Why Join Us?\n\n" +
                        "Tone: Professional but exciting.",
                jobTitle
        );

        try {
            // FIX 2: 'models' is a field, not a method (remove parentheses).
            // FIX 3: Pass 'null' for the configuration argument if you don't have specific configs.
            GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);

            // FIX 4: Use 'text()' to get the string response.
            return response.text();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating content from Gemini", e);
        }
    }
}