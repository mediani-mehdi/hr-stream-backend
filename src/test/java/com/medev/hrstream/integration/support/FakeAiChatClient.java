package com.medev.hrstream.integration.support;

import com.medev.hrstream.jobapplication.scoring.ai.AiChatClient;
import com.medev.hrstream.jobapplication.scoring.ai.AiChatResult;
import com.medev.hrstream.jobapplication.scoring.ai.exception.AllProvidersExhaustedException;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Primary
@Profile("integration-test")
@Component
public class FakeAiChatClient implements AiChatClient {

    private volatile Function<String, AiChatResult> behavior =
            prompt -> AiChatResult.builder()
                    .providerName("fake")
                    .rawResponse("{\"score\":80,\"reasoning\":\"fake ok\"}").build();
    private final AtomicInteger calls = new AtomicInteger();

    public void respondWithScore(int score, String reasoning) {
        behavior = prompt -> AiChatResult.builder()
                .providerName("fake")
                .rawResponse("{\"score\":" + score + ",\"reasoning\":\"" + reasoning + "\"}").build();
    }

    public void failAll() {
        behavior = prompt -> { throw new AllProvidersExhaustedException(List.of("fake"), null); };
    }

    public int callCount() { return calls.get(); }
    public void resetCalls() { calls.set(0); }

    @Override public String name() { return "fake"; }
    @Override public boolean isAvailable() { return true; }
    @Override public AiChatResult complete(String prompt) {
        calls.incrementAndGet();
        return behavior.apply(prompt);
    }
}
