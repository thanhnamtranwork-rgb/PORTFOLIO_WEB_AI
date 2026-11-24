package com.motelona.assistant.service;

import java.time.Duration;

/** One server-side text-generation provider in the ordered failover chain. */
public interface GenerativeModelProvider {
    String name();
    int priority();
    boolean isConfigured();
    String modelName();
    String answer(ModelPrompt prompt);

    /** Lets the router pass its remaining request budget to providers that support bounded calls. */
    default String answer(ModelPrompt prompt, Duration timeout) {
        return answer(prompt);
    }
}
