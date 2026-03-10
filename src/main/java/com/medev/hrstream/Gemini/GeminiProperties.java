package com.medev.hrstream.Gemini;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {

    @NotBlank
    private String apiKey;

    private Chat chat = new Chat();


    @Data
    public static class Chat {
        @NotBlank
        private String model = "gemini-1.5-flash"; // updated default fallback

    }
}