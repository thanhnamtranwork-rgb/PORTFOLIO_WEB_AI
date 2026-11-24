package com.motelona.assistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public final class GroqModelService extends OpenAiCompatibleModelService {
    public GroqModelService(ObjectMapper mapper,
            @Value("${motelona.providers.groq.api-key:}") String apiKey,
            @Value("${motelona.providers.groq.endpoint:https://api.groq.com/openai/v1/chat/completions}") String endpoint,
            @Value("${motelona.providers.groq.model:openai/gpt-oss-20b}") String model) {
        super(mapper, "groq", apiKey, endpoint, model, 20);
    }
}
