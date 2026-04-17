package com.medev.hrstream.jobapplication.scoring.ai;

import com.medev.hrstream.config.AiProviderProperties;
import com.medev.hrstream.jobapplication.scoring.ai.exception.AllProvidersExhaustedException;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderRateLimitedException;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderTransientException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailoverAiChatClientTest {

    @Test
    void returnsFirstAvailableProviderResult() {
        FakeClient openrouter = new FakeClient("openrouter", true, FakeBehavior.SUCCESS);
        FakeClient gemini = new FakeClient("gemini", true, FakeBehavior.SUCCESS);

        FailoverAiChatClient failover = buildWithOrder(
                List.of("openrouter", "gemini"), List.of(openrouter, gemini));

        AiChatResult result = failover.complete("hi");

        assertThat(result.getProviderName()).isEqualTo("openrouter");
        assertThat(openrouter.calls.get()).isEqualTo(1);
        assertThat(gemini.calls.get()).isZero();
    }

    @Test
    void skipsRateLimitedProvidersAndUsesNext() {
        FakeClient openrouter = new FakeClient("openrouter", true, FakeBehavior.RATE_LIMIT);
        FakeClient gemini = new FakeClient("gemini", true, FakeBehavior.SUCCESS);

        FailoverAiChatClient failover = buildWithOrder(
                List.of("openrouter", "gemini"), List.of(openrouter, gemini));

        AiChatResult result = failover.complete("hi");

        assertThat(result.getProviderName()).isEqualTo("gemini");
    }

    @Test
    void skipsUnavailableProviders() {
        FakeClient openrouter = new FakeClient("openrouter", false, FakeBehavior.SUCCESS);
        FakeClient gemini = new FakeClient("gemini", true, FakeBehavior.SUCCESS);

        FailoverAiChatClient failover = buildWithOrder(
                List.of("openrouter", "gemini"), List.of(openrouter, gemini));

        AiChatResult result = failover.complete("hi");

        assertThat(result.getProviderName()).isEqualTo("gemini");
        assertThat(openrouter.calls.get()).isZero();
    }

    @Test
    void throwsAllProvidersExhaustedWhenEveryoneFails() {
        FakeClient openrouter = new FakeClient("openrouter", true, FakeBehavior.RATE_LIMIT);
        FakeClient gemini = new FakeClient("gemini", true, FakeBehavior.TRANSIENT);

        FailoverAiChatClient failover = buildWithOrder(
                List.of("openrouter", "gemini"), List.of(openrouter, gemini));

        assertThatThrownBy(() -> failover.complete("hi"))
                .isInstanceOf(AllProvidersExhaustedException.class)
                .extracting(e -> ((AllProvidersExhaustedException) e).getAttempted())
                .isEqualTo(List.of("openrouter", "gemini"));
    }

    @Test
    void honoursConfiguredOrderIgnoringUnknownProviders() {
        FakeClient gemini = new FakeClient("gemini", true, FakeBehavior.SUCCESS);

        FailoverAiChatClient failover = buildWithOrder(
                List.of("unknown", "gemini"), List.of(gemini));

        AiChatResult result = failover.complete("hi");
        assertThat(result.getProviderName()).isEqualTo("gemini");
    }

    private FailoverAiChatClient buildWithOrder(List<String> order, List<AiChatClient> clients) {
        AiProviderProperties props = new AiProviderProperties();
        props.getProviders().setOrder(order);
        return new FailoverAiChatClient(clients, props);
    }

    enum FakeBehavior { SUCCESS, RATE_LIMIT, TRANSIENT }

    static class FakeClient implements AiChatClient {
        final String name;
        final boolean available;
        final FakeBehavior behavior;
        final AtomicInteger calls = new AtomicInteger();

        FakeClient(String name, boolean available, FakeBehavior behavior) {
            this.name = name;
            this.available = available;
            this.behavior = behavior;
        }
        @Override public String name() { return name; }
        @Override public boolean isAvailable() { return available; }
        @Override public AiChatResult complete(String prompt) {
            calls.incrementAndGet();
            switch (behavior) {
                case SUCCESS:
                    return AiChatResult.builder().providerName(name).rawResponse("{\"score\":75,\"reasoning\":\"ok\"}").build();
                case RATE_LIMIT:
                    throw new ProviderRateLimitedException(name, "429");
                case TRANSIENT:
                    throw new ProviderTransientException(name, "boom", null);
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
