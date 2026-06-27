package com.medev.hrstream.AimodelService.lmstudio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lmstudio")
public class LMStudioProperties {
    private String baseUrl = "http://localhost:1234";
    private String apiKey;
    private String model = "lm-studio-community/Meta-Llama-3.2-3B-Instruct";
    private long timeoutMs = 30000;
}
