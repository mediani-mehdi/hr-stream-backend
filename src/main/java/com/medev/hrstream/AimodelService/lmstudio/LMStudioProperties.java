package com.medev.hrstream.AimodelService.lmstudio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lmstudio")
public class LMStudioProperties {
    private String baseUrl = "http://localhost:1234";
    private String apiKey = "test";
    private String model = "lnvidia/nemotron-3-nano-4b";
    private long timeoutMs = 30000;
}
