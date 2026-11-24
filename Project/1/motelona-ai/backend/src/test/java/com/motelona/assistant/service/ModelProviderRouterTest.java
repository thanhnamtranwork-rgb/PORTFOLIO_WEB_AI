package com.motelona.assistant.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelProviderRouterTest {
    @Test void fallsThroughToNextConfiguredProviderAndReportsTheProviderThatAnswered() {
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger secondCalls = new AtomicInteger();
        GenerativeModelProvider first = provider("gemini", 10, () -> {
            firstCalls.incrementAndGet();
            throw new AiUnavailableException("quota");
        });
        GenerativeModelProvider second = provider("groq", 20, () -> {
            secondCalls.incrementAndGet();
            return "backup answer";
        });
        var router = new ModelProviderRouter(List.of(second, first));

        var answer = router.answer(new ModelPrompt("system", "user", 128));

        assertEquals("groq", answer.provider());
        assertEquals("backup answer", answer.text());
        assertEquals(1, firstCalls.get());
        assertEquals(1, secondCalls.get());
    }

    @Test void skipsProvidersWithNoConfiguredKey() {
        AtomicInteger calls = new AtomicInteger();
        GenerativeModelProvider disabled = new GenerativeModelProvider() {
            public String name() { return "disabled"; }
            public int priority() { return 10; }
            public boolean isConfigured() { return false; }
            public String modelName() { return "none"; }
            public String answer(ModelPrompt prompt) { calls.incrementAndGet(); return "unexpected"; }
        };
        var router = new ModelProviderRouter(List.of(disabled));

        assertFalse(router.isConfigured());
        assertThrows(AiUnavailableException.class, () -> router.answer(new ModelPrompt("system", "user", 128)));
        assertEquals(0, calls.get());
    }

    @Test void safetyRefusalStopsWithoutSendingPromptToAnotherProvider() {
        AtomicInteger secondCalls = new AtomicInteger();
        GenerativeModelProvider first = provider("gemini", 10, () -> {
            throw new ModelRefusalException("safety refusal");
        });
        GenerativeModelProvider second = provider("groq", 20, () -> {
            secondCalls.incrementAndGet();
            return "must not be called";
        });
        var router = new ModelProviderRouter(List.of(first, second));

        assertThrows(ModelRefusalException.class, () -> router.answer(new ModelPrompt("system", "user", 128)));
        assertEquals(0, secondCalls.get());
    }

    private GenerativeModelProvider provider(String name, int priority, Answer answer) {
        return new GenerativeModelProvider() {
            public String name() { return name; }
            public int priority() { return priority; }
            public boolean isConfigured() { return true; }
            public String modelName() { return name + "-model"; }
            public String answer(ModelPrompt prompt) { return answer.get(); }
        };
    }

    @FunctionalInterface private interface Answer { String get(); }
}
