package com.medev.hrstream.jobapplication.scoring.ai;

public interface AiChatClient {
    /** Unique name ("openrouter", "gemini", "claude", "glm"). */
    String name();

    /** Returns true if the adapter is configured and healthy enough to try. */
    boolean isAvailable();

    /** Sends the prompt; returns raw text. Must throw a Provider*Exception on failure. */
    AiChatResult complete(String prompt);
}
