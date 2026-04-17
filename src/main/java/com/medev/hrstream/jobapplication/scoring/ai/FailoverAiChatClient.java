package com.medev.hrstream.jobapplication.scoring.ai;

import com.medev.hrstream.config.AiProviderProperties;
import com.medev.hrstream.jobapplication.scoring.ai.exception.AllProvidersExhaustedException;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderQuotaExceededException;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderRateLimitedException;
import com.medev.hrstream.jobapplication.scoring.ai.exception.ProviderTransientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
@Profile("!integration-test")
public class FailoverAiChatClient implements AiChatClient {

    private static final Logger log = LoggerFactory.getLogger(FailoverAiChatClient.class);

    private final Map<String, AiChatClient> byName;
    private final AiProviderProperties props;

    public FailoverAiChatClient(List<AiChatClient> clients, AiProviderProperties props) {
        this.byName = new LinkedHashMap<>();
        for (AiChatClient c : clients) {
            if (c instanceof FailoverAiChatClient) continue;
            byName.put(c.name(), c);
        }
        this.props = props;
    }

    @Override public String name() { return "failover"; }

    @Override public boolean isAvailable() {
        for (String n : props.getProviders().getOrder()) {
            AiChatClient c = byName.get(n);
            if (c != null && c.isAvailable()) return true;
        }
        return false;
    }

    @Override
    public AiChatResult complete(String prompt) {
        List<String> attempted = new ArrayList<>();
        Throwable last = null;
        for (String providerName : props.getProviders().getOrder()) {
            AiChatClient client = byName.get(providerName);
            if (client == null || !client.isAvailable()) continue;
            attempted.add(providerName);
            try {
                log.debug("ai-failover: trying provider {}", providerName);
                return client.complete(prompt);
            } catch (ProviderRateLimitedException
                     | ProviderQuotaExceededException
                     | ProviderTransientException e) {
                log.warn("ai-failover: {} failed ({}), trying next", providerName, e.getClass().getSimpleName());
                last = e;
            } catch (RuntimeException e) {
                log.warn("ai-failover: {} threw unexpected {}, continuing", providerName, e.toString());
                last = e;
            }
        }
        throw new AllProvidersExhaustedException(attempted, last);
    }
}
