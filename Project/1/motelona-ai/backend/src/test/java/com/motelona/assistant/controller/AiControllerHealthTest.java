package com.motelona.assistant.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.motelona.assistant.ai.LocalAiEngine;
import com.motelona.assistant.search.SearchProvider;
import com.motelona.assistant.service.AiChatService;
import com.motelona.assistant.service.RateLimitService;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiControllerHealthTest {
    @Test
    void reportsLocalEngineReadyWithoutApiKey() {
        SearchProvider searchProvider = mock(SearchProvider.class);
        when(searchProvider.isEnabled()).thenReturn(true);
        AiController controller = new AiController(mock(AiChatService.class), mock(RateLimitService.class), searchProvider);
        Map<String, Object> health = controller.health();
        assertEquals("up", health.get("backend"));
        assertEquals("ready", health.get("model"));
        assertEquals("java-local", health.get("provider"));
        assertEquals("self-built-nlp", health.get("engine"));
        assertEquals(LocalAiEngine.ENGINE_VERSION, health.get("engineVersion"));
        assertEquals(false, health.get("apiKeyRequired"));
        assertEquals(false, health.get("apiKeyConfigured"));
        assertTrue(String.valueOf(health.get("message")).contains("without an external AI API key"));
        assertTrue((Boolean) health.get("internetSearchEnabled"));
        assertEquals("DuckDuckGo HTML (no API key)", health.get("internetSearchProvider"));
    }
}
