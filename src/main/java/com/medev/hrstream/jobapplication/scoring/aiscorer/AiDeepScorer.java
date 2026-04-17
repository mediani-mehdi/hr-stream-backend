package com.medev.hrstream.jobapplication.scoring.aiscorer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;
import com.medev.hrstream.jobapplication.scoring.ai.AiChatClient;
import com.medev.hrstream.jobapplication.scoring.ai.AiChatResult;
import com.medev.hrstream.jobapplication.scoring.ai.exception.AllProvidersExhaustedException;
import org.springframework.stereotype.Component;

@Component
public class AiDeepScorer {

    private final AiChatClient chat;
    private final ScoringPromptBuilder promptBuilder;
    private final ObjectMapper mapper;

    public AiDeepScorer(AiChatClient chat, ScoringPromptBuilder promptBuilder, ObjectMapper mapper) {
        this.chat = chat;
        this.promptBuilder = promptBuilder;
        this.mapper = mapper;
    }

    public AiScoreResult score(AiScorerInput input) {
        String prompt = promptBuilder.build(input);
        AiChatResult chatResult;
        try {
            chatResult = chat.complete(prompt);
        } catch (AllProvidersExhaustedException e) {
            throw new AiScoringFailedException(
                    ProcessingErrorCode.AI_PROVIDERS_EXHAUSTED, "all providers failed", e);
        }

        String raw = chatResult.getRawResponse();
        String json = extractJson(raw);
        try {
            JsonNode node = mapper.readTree(json);
            int rawScore = node.path("score").asInt(-1);
            String reasoning = node.path("reasoning").asText("");
            if (rawScore < 0 && reasoning.isEmpty()) {
                throw new AiScoringFailedException(
                        ProcessingErrorCode.AI_RESPONSE_MALFORMED, "missing score and reasoning");
            }
            int clamped = Math.max(0, Math.min(100, rawScore));
            return AiScoreResult.builder()
                    .score(clamped)
                    .reasoning(reasoning)
                    .providerName(chatResult.getProviderName())
                    .build();
        } catch (JsonProcessingException e) {
            throw new AiScoringFailedException(
                    ProcessingErrorCode.AI_RESPONSE_MALFORMED, "could not parse AI JSON: " + raw, e);
        }
    }

    /** Pulls the first {...} block out of the raw text. */
    private String extractJson(String raw) {
        if (raw == null) return "{}";
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }
}
