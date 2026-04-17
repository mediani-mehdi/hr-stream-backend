package com.medev.hrstream.jobapplication.scoring.ai;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AiChatResult {
    String providerName;
    String rawResponse;
}
