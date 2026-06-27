package com.medev.hrstream.AimodelService.ollama;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ollama")
public class OllamaProperties {
    private String baseUrl = "http://localhost:11434";
    private String model = "llama3.2";
    private long timeoutMs = 30000;
}
