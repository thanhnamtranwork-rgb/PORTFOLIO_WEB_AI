package com.motelona.assistant.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntentClassifierTest {
    private final IntentClassifier classifier = new IntentClassifier();

    @Test void recognizesVietnameseSkillQuestions() {
        assertEquals(IntentClassifier.Intent.SKILLS, classifier.classify("Bạn có những kỹ năng nào?").intent());
    }
    @Test void recognizesEducationQuestions() {
        assertEquals(IntentClassifier.Intent.EDUCATION, classifier.classify("Bạn đã học ở trường nào?").intent());
    }
    @Test void doesNotForceUnrelatedQuestionsIntoPortfolioIntents() {
        assertEquals(IntentClassifier.Intent.GENERAL, classifier.classify("Ai sẽ vô địch giải đấu năm tới?").intent());
    }
}
