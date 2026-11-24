package com.motelona.assistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public final class OpenRouterModelService extends OpenAiCompatibleModelService {
    public OpenRouterModelService(ObjectMapper mapper,
            @Value("${motelona.providers.openrouter.api-key:}") String apiKey,
            @Value("${motelona.providers.openrouter.endpoint:https://openrouter.ai/api/v1/chat/completions}") String endpoint,
            @Value("${motelona.providers.openrouter.model:openrouter/free}") String model) {
        super(mapper, "openrouter", apiKey, endpoint, model, 30);
    }
}
