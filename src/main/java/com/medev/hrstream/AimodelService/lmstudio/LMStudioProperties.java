package com.medev.hrstream.AimodelService.lmstudio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lmstudio")
public class LMStudioProperties {
    private String baseUrl = "http://127.0.0.1:1234";
    private String apiKey = "test";
    private String model = "qwen2.5-coder-0.5b-instruct";
    private long timeoutMs = 30000;
}
