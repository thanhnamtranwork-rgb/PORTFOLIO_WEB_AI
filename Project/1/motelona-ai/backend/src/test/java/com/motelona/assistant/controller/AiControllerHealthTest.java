package com.motelona.assistant.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.motelona.assistant.search.SearchProvider;
import com.motelona.assistant.service.AiChatService;
import com.motelona.assistant.service.RateLimitService;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiControllerHealthTest {
    @Test
    void reportsLocalModelAndWebSearchStatus() {
        SearchProvider searchProvider = mock(SearchProvider.class);
        when(searchProvider.isEnabled()).thenReturn(true);
        AiChatService aiChatService = mock(AiChatService.class);
        when(aiChatService.modelName()).thenReturn("gemini-3.8-flash");
        when(aiChatService.activeProvider()).thenReturn("gemini");
        when(aiChatService.isAiConfigured()).thenReturn(true);
        when(aiChatService.isChatReady()).thenReturn(true);
        when(aiChatService.isInternetSearchEnabled()).thenReturn(true);
        when(aiChatService.webSearchProvider()).thenReturn("Public web search and source reading");
        AiController controller = new AiController(aiChatService, mock(RateLimitService.class), searchProvider);
        Map<String, Object> health = controller.health();
        assertEquals("up", health.get("backend"));
        assertEquals("gemini-3.8-flash", health.get("model"));
        assertEquals("gemini", health.get("provider"));
        assertEquals("model-provider-chain", health.get("engine"));
        assertEquals("Scope guard + Gemini/Groq/OpenRouter failover + local knowledge fallback", health.get("engineVersion"));
        assertEquals(false, health.get("apiKeyRequired"));
        assertTrue((Boolean) health.get("modelConfigured"));
        assertTrue((Boolean) health.get("chatReady"));
        assertTrue(String.valueOf(health.get("message")).contains("scope checks run before model requests"));
        assertTrue((Boolean) health.get("internetSearchEnabled"));
        assertTrue(String.valueOf(health.get("internetSearchProvider")).contains("Public web search"));
    }
}
