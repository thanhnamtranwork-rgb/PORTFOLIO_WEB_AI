package com.motelona.assistant.service;

import com.motelona.assistant.ai.IntentClassifier;
import com.motelona.assistant.ai.TextVectorizer;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Keeps unrequested off-topic questions from consuming a hosted model request. */
@Component
public class ScopeGuard {
    private static final Set<String> TOPIC_TERMS = Set.of(
            "motelona", "portfolio", "website", "web site", "trang web", "trang nay", "website nay",
            "du an", "project", "code", "source code", "ma nguon", "gemini", "java", "spring boot",
            "mongodb", "mongo db", "database", "co so du lieu", "javascript", "html", "css", "python",
            "frontend", "backend", "full stack", "ai assistant", "tro ly", "ky nang", "skills",
            "hoc van", "education", "experience", "kinh nghiem", "ho so", "profile", "cv", "resume",
            "lien he", "contact", "api", "deploy", "cai dat", "cau hinh", "mo hinh ai", "model ai",
            "groq", "openrouter", "model provider", "llm", "readme", "tai lieu", "document", "class",
            "controller", "service", "gpt oss");
    private static final IntentClassifier INTENTS = new IntentClassifier();

    public boolean allows(String question, String conversationHistory, boolean explicitWebSearch) {
        if (explicitWebSearch) return true;
        if (mentionsProjectTopic(question) || isPortfolioIntent(question)) return true;
        return followsPortfolioTurn(conversationHistory);
    }

    public String redirectMessage(String language) {
        return "vi".equalsIgnoreCase(language)
                ? "Mình tập trung hỗ trợ Portfolio này và dự án MoteLona: hồ sơ, kỹ năng, dự án, Java/MongoDB/AI và cách website hoạt động. Câu hỏi hiện chưa liên quan đến phạm vi đó. Bạn có thể hỏi về website/dự án, hoặc bật Tìm web để tra cứu chủ đề khác."
                : "I focus on this Portfolio and the MoteLona project: its profile, skills, projects, Java/MongoDB/AI stack, and how the website works. This question appears outside that scope. Ask about the site or project, or enable Web search for another topic.";
    }

    private boolean mentionsProjectTopic(String question) {
        String normalized = " " + TextVectorizer.normalize(question) + " ";
        return TOPIC_TERMS.stream().anyMatch(term -> normalized.contains(" " + term + " "));
    }

    private boolean isPortfolioIntent(String question) {
        var classification = INTENTS.classify(question);
        return classification.intent() != IntentClassifier.Intent.GENERAL && classification.confidence() >= 0.20;
    }

    private boolean followsPortfolioTurn(String history) {
        if (history == null || history.isBlank()) return false;
        String[] lines = history.split("\\R");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.regionMatches(true, 0, "user:", 0, 5)) continue;
            String previousQuestion = line.substring(5).trim();
            return mentionsProjectTopic(previousQuestion) || isPortfolioIntent(previousQuestion);
        }
        return false;
    }
}
