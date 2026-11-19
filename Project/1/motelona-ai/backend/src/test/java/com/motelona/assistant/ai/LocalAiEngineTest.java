package com.motelona.assistant.ai;

import com.motelona.assistant.service.GlossaryService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalAiEngineTest {
    private final LocalAiEngine engine = new LocalAiEngine(new GlossaryService());

    @Test
    void greetsInVietnameseWithoutExternalModel() {
        String answer = engine.chat("Xin chào", "vi", "", "");
        assertTrue(answer.contains("MoteLona"));
        assertTrue(answer.contains("Java"));
    }

    @Test
    void retrievesSkillsFromSuppliedPortfolioContext() {
        String context = "Skills: skillName: Java; categoryName: Backend; proficiencyLevel: Intermediate\n"
                + "Projects: title: Portfolio; shortDescription: Personal website";
        String answer = engine.chat("Bạn có những kỹ năng nào?", "vi", context, "");
        assertTrue(answer.contains("Java"));
        assertTrue(answer.contains("kỹ năng"));
    }

    @Test
    void doesNotInventOwnerFactsWhenContextIsMissing() {
        String answer = engine.chat("Bạn đã học ở trường nào?", "vi", "", "");
        assertTrue(answer.contains("chưa có nội dung") || answer.contains("chưa tìm thấy"));
        assertFalse(answer.contains("Đại học Bách khoa"));
    }

    @Test
    void explainsKnownLocalGlossaryTerm() {
        String answer = engine.explain("TF-IDF là gì?", "basic", "vi", "");
        assertTrue(answer.contains("TF-IDF"));
        assertTrue(answer.contains("tần suất"));
    }

    @Test
    void clearlyDisclosesUnknownGeneralQuestionLimits() {
        String answer = engine.chat("Ai sẽ vô địch giải đấu năm tới?", "vi", "", "");
        assertTrue(answer.contains("không đoán"));
        assertTrue(answer.contains("Tìm trên Internet"));
    }
}
