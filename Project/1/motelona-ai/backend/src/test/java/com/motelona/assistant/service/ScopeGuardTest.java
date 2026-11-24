package com.motelona.assistant.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScopeGuardTest {
    private final ScopeGuard guard = new ScopeGuard();

    @Test void unrelatedQuestionIsBlockedWithoutEvidenceOrExplicitSearch() {
        assertFalse(guard.allows("Viết cho tôi một bài thơ về mèo", "", false));
        assertFalse(guard.allows("What is the weather in Hanoi today?", "", false));
    }

    @Test void portfolioAndProjectTopicsRemainAvailable() {
        assertTrue(guard.allows("Theo README của dự án, model Gemini là gì?", "", false));
        assertTrue(guard.allows("How does OpenRouter fail over in this project?", "", false));
        assertTrue(guard.allows("Thêm một câu hỏi tiếp theo", "user: Bạn học trường nào?", false));
    }

    @Test void explicitWebSearchLetsTheUserAskOutsideThePortfolioScope() {
        assertTrue(guard.allows("Thời tiết hôm nay thế nào?", "", true));
    }
}
