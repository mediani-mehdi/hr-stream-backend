package com.medev.hrstream.jobapplication.scoring.aiscorer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medev.hrstream.config.ScoringProperties;
import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;
import com.medev.hrstream.jobapplication.scoring.ai.AiChatClient;
import com.medev.hrstream.jobapplication.scoring.ai.AiChatResult;
import com.medev.hrstream.jobapplication.scoring.ai.exception.AllProvidersExhaustedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiDeepScorerTest {

    private final ScoringPromptBuilder promptBuilder = new ScoringPromptBuilder(new ScoringProperties());
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsesValidJsonResponse() {
        AiChatClient chat = mock(AiChatClient.class);
        when(chat.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(AiChatResult.builder().providerName("openrouter")
                        .rawResponse("{\"score\":82,\"reasoning\":\"strong match\"}").build());

        AiDeepScorer scorer = new AiDeepScorer(chat, promptBuilder, mapper);
        AiScoreResult result = scorer.score(validInput());

        assertThat(result.getScore()).isEqualTo(82);
        assertThat(result.getReasoning()).isEqualTo("strong match");
        assertThat(result.getProviderName()).isEqualTo("openrouter");
    }

    @Test
    void clampsScoreAbove100ToHundred() {
        AiChatClient chat = mock(AiChatClient.class);
        when(chat.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(AiChatResult.builder().providerName("p")
                        .rawResponse("{\"score\":150,\"reasoning\":\"x\"}").build());

        AiDeepScorer scorer = new AiDeepScorer(chat, promptBuilder, mapper);
        assertThat(scorer.score(validInput()).getScore()).isEqualTo(100);
    }

    @Test
    void clampsNegativeScoreToZero() {
        AiChatClient chat = mock(AiChatClient.class);
        when(chat.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(AiChatResult.builder().providerName("p")
                        .rawResponse("{\"score\":-5,\"reasoning\":\"x\"}").build());

        AiDeepScorer scorer = new AiDeepScorer(chat, promptBuilder, mapper);
        assertThat(scorer.score(validInput()).getScore()).isZero();
    }

    @Test
    void extractsJsonFromResponseWithExtraProse() {
        AiChatClient chat = mock(AiChatClient.class);
        when(chat.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(AiChatResult.builder().providerName("p")
                        .rawResponse("Sure! Here you go:\n{\"score\":70,\"reasoning\":\"ok\"}\nHope that helps.")
                        .build());

        AiDeepScorer scorer = new AiDeepScorer(chat, promptBuilder, mapper);
        assertThat(scorer.score(validInput()).getScore()).isEqualTo(70);
    }

    @Test
    void throwsAiResponseMalformedWhenJsonUnparseable() {
        AiChatClient chat = mock(AiChatClient.class);
        when(chat.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(AiChatResult.builder().providerName("p")
                        .rawResponse("not json at all").build());

        AiDeepScorer scorer = new AiDeepScorer(chat, promptBuilder, mapper);
        assertThatThrownBy(() -> scorer.score(validInput()))
                .isInstanceOf(AiScoringFailedException.class)
                .extracting(e -> ((AiScoringFailedException) e).getCode())
                .isEqualTo(ProcessingErrorCode.AI_RESPONSE_MALFORMED);
    }

    @Test
    void throwsAiProvidersExhaustedWhenChatFails() {
        AiChatClient chat = mock(AiChatClient.class);
        when(chat.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new AllProvidersExhaustedException(List.of("openrouter"), null));

        AiDeepScorer scorer = new AiDeepScorer(chat, promptBuilder, mapper);
        assertThatThrownBy(() -> scorer.score(validInput()))
                .isInstanceOf(AiScoringFailedException.class)
                .extracting(e -> ((AiScoringFailedException) e).getCode())
                .isEqualTo(ProcessingErrorCode.AI_PROVIDERS_EXHAUSTED);
    }

    private AiScorerInput validInput() {
        return AiScorerInput.builder()
                .jobTitle("Java Engineer")
                .jobDescription("Build APIs")
                .requiredSkills(List.of("Java"))
                .niceToHaveSkills(List.of())
                .experienceLevel("senior")
                .cvText("10 years Java")
                .build();
    }
}
