package com.motelona.assistant.ai;

import com.motelona.assistant.ai.IntentClassifier.Classification;
import com.motelona.assistant.ai.IntentClassifier.Intent;
import com.motelona.assistant.service.GlossaryService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Self-built, API-free assistant engine: intent classification, TF-IDF retrieval and deterministic response composition. */
@Service
public class LocalAiEngine {
    public static final String ENGINE_VERSION = "MoteLona Local NLP Engine 1.1";
    private final IntentClassifier classifier = new IntentClassifier();
    private final KnowledgeRetrievalService retriever = new KnowledgeRetrievalService();
    private final GlossaryService glossary;

    public LocalAiEngine(GlossaryService glossary) { this.glossary = glossary; }

    public String chat(String message, String language, String websiteContext, String memoryContext) {
        return chat(message, language, websiteContext, memoryContext, "");
    }

    public String chat(String message, String language, String websiteContext, String memoryContext, String history) {
        boolean vi = "vi".equalsIgnoreCase(language);
        String retrievalQuestion = message;
        Classification classification = classifier.classify(message);
        if (classification.intent() == Intent.GENERAL && isFollowUp(message)) {
            String previousQuestion = lastUserQuestion(history);
            if (!previousQuestion.isBlank()) {
                retrievalQuestion = previousQuestion + " " + message;
                classification = classifier.classify(previousQuestion);
            }
        }
        Intent intent = classification.intent();
        if (intent == Intent.GREETING) return vi
                ? "Xin chào! Mình là MoteLona, trợ lý Java chạy bằng thuật toán xử lý ngôn ngữ cục bộ. Bạn có thể hỏi về hồ sơ, kỹ năng, học vấn, kinh nghiệm, dự án hoặc yêu cầu giải thích thuật ngữ."
                : "Hello! I’m MoteLona, a Java assistant powered by local language-processing algorithms. Ask about the portfolio, skills, education, experience, projects, or a technical term.";
        if (intent == Intent.THANKS) return vi ? "Rất vui được hỗ trợ bạn! Bạn có thể hỏi tiếp về nội dung Portfolio." : "You’re welcome! Feel free to ask another question about the portfolio.";
        if (intent == Intent.CAPABILITIES) return vi
                ? "Mình có thể tra cứu nội dung Portfolio/MySQL, tìm đoạn phù hợp, giải thích thuật ngữ, đưa bạn đến trang hoặc mục liên quan và tô vàng cụm cần xem. Khi bạn chủ động bật Tìm web hoặc yêu cầu ‘Tìm trên Internet: …’, mình truy vấn trang HTML công khai của DuckDuckGo rồi hiển thị tiêu đề, trích đoạn và liên kết nguồn. Phần AI chạy bằng Java cục bộ, không cần khóa API AI."
                : "I can search Portfolio/MySQL content, find relevant passages, explain technical terms, navigate to related pages or sections, and highlight the phrase to review. When you enable Search web or ask ‘Search the web for …’, I query DuckDuckGo's public HTML results page and show titles, snippets, and source links. The assistant logic runs locally in Java; no AI API key is required.";

        if (asksForDefinition(message)) {
            GlossaryService.Definition definition = glossary.find(message);
            if (definition != null) return definitionText(definition, vi, "basic");
        }

        String combinedContext = (websiteContext == null ? "" : websiteContext.trim());
        String safeMemory = memoryContext == null ? "" : memoryContext.trim();
        if (!safeMemory.isBlank()) combinedContext += "\nUser-authorized memory reference: " + safeMemory;
        List<KnowledgeRetrievalService.Passage> passages = retriever.retrieve(retrievalQuestion, combinedContext, intent, 4);
        double threshold = intent == Intent.GENERAL ? 0.055 : 0.045;
        List<KnowledgeRetrievalService.Passage> relevant = passages.stream().filter(p -> p.score() >= threshold).toList();

        if (!relevant.isEmpty()) return groundedAnswer(relevant, intent, vi);
        if (intent == Intent.CV || intent == Intent.CONTACT) return vi
                ? "Mình chưa tìm thấy đường dẫn hoặc thông tin liên hệ tương ứng trong dữ liệu Portfolio hiện có. Bạn có thể kiểm tra trực tiếp mục Liên hệ hoặc CV trên trang."
                : "I could not find the requested contact or CV link in the available Portfolio data. Please check the Contact or CV section on the page.";
        if (intent != Intent.GENERAL) return vi
                ? "Mình nhận diện câu hỏi đang hỏi về " + intentLabel(intent, true) + ", nhưng dữ liệu được cung cấp chưa có nội dung đủ liên quan để trả lời chính xác. Bạn có thể bổ sung dữ liệu trong Portfolio/MySQL rồi hỏi lại."
                : "I recognized a question about " + intentLabel(intent, false) + ", but the supplied data does not contain enough relevant information to answer accurately. Add the missing information to the Portfolio/MySQL data and ask again.";

        GlossaryService.Definition mentioned = glossary.findMentioned(message).stream().findFirst().orElse(null);
        if (mentioned != null) return definitionText(mentioned, vi, "basic");
        return vi
                ? "Mình chưa có đủ nguồn đáng tin cậy để trả lời câu này, nên sẽ không đoán. Bạn có thể bật nút Tìm web hoặc yêu cầu ‘Tìm trên Internet: [câu hỏi]’ để xem các nguồn công khai. Lưu ý: MoteLona truy xuất tiêu đề/trích đoạn từ công cụ tìm kiếm, không phải LLM tổng quát và không tự xác minh toàn bộ nội dung của trang ngoài."
                : "I do not have enough reliable evidence to answer, so I will not guess. Enable Search web or ask ‘Search the web for [question]’ to review public sources. MoteLona retrieves search titles and snippets; it is not a general-purpose LLM and does not independently verify every external page.";
    }

    public String explain(String selectedText, String level, String language, String websiteContext) {
        boolean vi = "vi".equalsIgnoreCase(language);
        String text = selectedText == null ? "" : selectedText.trim();
        List<GlossaryService.Definition> definitions = glossary.findMentioned(text);
        if (definitions.isEmpty()) {
            GlossaryService.Definition exact = glossary.find(text);
            if (exact != null) definitions = List.of(exact);
        }
        if (!definitions.isEmpty()) {
            String details = definitions.stream().limit(5)
                    .map(d -> "• " + d.term() + ": " + (vi ? d.vi() : d.en()))
                    .collect(Collectors.joining("\n"));
            String prefix = vi ? "Các khái niệm có trong nội dung bạn chọn:\n" : "Concepts found in the selected text:\n";
            String depth = normalizedLevel(level);
            if ("technical".equals(depth) || "deep".equals(depth)) details += vi
                    ? "\n\nGóc nhìn kỹ thuật: hãy xem xét vai trò của từng thành phần, dữ liệu đầu vào/đầu ra, giới hạn, trường hợp lỗi và cách kiểm thử trong hệ thống thực tế."
                    : "\n\nTechnical view: consider each component’s role, inputs/outputs, limits, failure cases, and how it is tested in a real system.";
            if ("deep".equals(depth)) details += vi
                    ? "\n\nGiới hạn: phần giải thích dựa trên từ điển tích hợp và không thay thế việc đọc tài liệu chính thức hoặc kiểm chứng triển khai cụ thể."
                    : "\n\nLimit: this explanation is based on the built-in glossary and does not replace official documentation or verification of a specific implementation.";
            return prefix + details;
        }
        List<KnowledgeRetrievalService.Passage> matches = retriever.retrieve(text, websiteContext, Intent.GENERAL, 3);
        if (!matches.isEmpty()) return vi
                ? "Mình tìm thấy ngữ cảnh liên quan trên website:\n" + matches.stream().map(p -> "• " + p.text()).collect(Collectors.joining("\n")) + "\n\nMình có thể chỉ ra nội dung liên quan, nhưng từ điển hiện chưa có định nghĩa chuyên ngành đủ chắc chắn cho đoạn này."
                : "I found related website context:\n" + matches.stream().map(p -> "• " + p.text()).collect(Collectors.joining("\n")) + "\n\nI can identify relevant content, but the built-in glossary does not yet contain a reliable technical definition for this passage.";
        return vi
                ? "Từ điển cục bộ chưa có thuật ngữ phù hợp với đoạn bạn chọn. Hãy thử chọn một thuật ngữ cụ thể hoặc bổ sung định nghĩa vào GlossaryService.java."
                : "The local glossary has no matching term for this selection. Select a specific term or add a definition to GlossaryService.java.";
    }

    private String groundedAnswer(List<KnowledgeRetrievalService.Passage> passages, Intent intent, boolean vi) {
        String intro = switch (intent) {
            case PROFILE -> vi ? "Theo thông tin hồ sơ được công khai trên Portfolio:" : "According to the public profile on this Portfolio:";
            case SKILLS -> vi ? "Theo danh sách kỹ năng/công nghệ trên Portfolio:" : "According to the skills and technologies listed on the Portfolio:";
            case PROJECTS -> vi ? "Dữ liệu dự án hiện có trên Portfolio:" : "The project information currently available on the Portfolio:";
            case EDUCATION -> vi ? "Theo dữ liệu học vấn trên Portfolio:" : "According to the education data on the Portfolio:";
            case EXPERIENCE -> vi ? "Theo dữ liệu kinh nghiệm trên Portfolio:" : "According to the experience data on the Portfolio:";
            case CONTACT -> vi ? "Thông tin liên hệ tìm thấy trong dữ liệu Portfolio:" : "Contact information found in the Portfolio data:";
            case CV -> vi ? "Thông tin CV tìm thấy trong dữ liệu Portfolio:" : "CV information found in the Portfolio data:";
            default -> vi ? "Mình tìm thấy thông tin liên quan trong dữ liệu được cung cấp:" : "I found relevant information in the supplied data:";
        };
        String body = passages.stream().limit(3).map(p -> "• " + p.text()).collect(Collectors.joining("\n"));
        String note = vi
                ? "\n\nMình chỉ tổng hợp dữ liệu đang có, không tự suy đoán những thông tin chưa được cung cấp."
                : "\n\nI only summarize the available data and do not infer details that were not supplied.";
        return intro + "\n" + body + note;
    }

    private String definitionText(GlossaryService.Definition d, boolean vi, String level) {
        return (vi ? d.term() + " là: " + d.vi() : d.term() + ": " + d.en())
                + ("technical".equals(level) || "deep".equals(level)
                ? (vi ? "\n\nGóc nhìn kỹ thuật: hãy xác định vai trò, đầu vào, đầu ra và giới hạn của khái niệm trong hệ thống cụ thể."
                      : "\n\nTechnical view: identify the concept’s role, inputs, outputs, and limits in the specific system.") : "");
    }


    private boolean isFollowUp(String message) {
        String normalized = TextVectorizer.normalize(message);
        return normalized.length() < 50 && (normalized.contains("chi tiet hon") || normalized.contains("noi ro hon")
                || normalized.contains("ke them") || normalized.contains("more details") || normalized.contains("tell me more")
                || normalized.contains("go on") || normalized.equals("why") || normalized.equals("how"));
    }

    private String lastUserQuestion(String history) {
        if (history == null || history.isBlank()) return "";
        String[] lines = history.split("\\R");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.regionMatches(true, 0, "user:", 0, 5)) return line.substring(5).trim();
        }
        return "";
    }

    private boolean asksForDefinition(String text) {
        String q = TextVectorizer.normalize(text);
        return q.contains("la gi") || q.contains("nghia la") || q.contains("giai thich") || q.contains("what is")
                || q.startsWith("define ") || q.startsWith("explain ");
    }
    private String normalizedLevel(String level) {
        String value = TextVectorizer.normalize(level).toLowerCase(Locale.ROOT);
        if (value.contains("deep") || value.contains("chuyen sau")) return "deep";
        if (value.contains("technical") || value.contains("ky thuat")) return "technical";
        return "basic";
    }
    private String intentLabel(Intent intent, boolean vi) {
        if (!vi) return switch (intent) {
            case PROFILE -> "the profile"; case SKILLS -> "skills and technologies"; case PROJECTS -> "projects";
            case EDUCATION -> "education"; case EXPERIENCE -> "experience"; case CONTACT -> "contact information";
            case CV -> "the CV"; default -> "the requested topic";
        };
        return switch (intent) {
            case PROFILE -> "hồ sơ cá nhân"; case SKILLS -> "kỹ năng và công nghệ"; case PROJECTS -> "các dự án";
            case EDUCATION -> "học vấn"; case EXPERIENCE -> "kinh nghiệm"; case CONTACT -> "thông tin liên hệ";
            case CV -> "CV"; default -> "chủ đề bạn hỏi";
        };
    }
}
