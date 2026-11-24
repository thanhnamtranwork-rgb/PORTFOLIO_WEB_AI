package com.motelona.assistant.ai;

import com.motelona.assistant.ai.IntentClassifier.Classification;
import com.motelona.assistant.ai.IntentClassifier.Intent;
import com.motelona.assistant.service.GlossaryService;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Provides deterministic, Portfolio-grounded replies while the hosted model is not configured. */
@Service
public class LocalAiEngine {
    public static final String ENGINE_VERSION = "MoteLona local knowledge fallback 1.1";
    private final IntentClassifier classifier = new IntentClassifier();
    private final KnowledgeRetrievalService retriever = new KnowledgeRetrievalService();
    private final GlossaryService glossary;

    public LocalAiEngine(GlossaryService glossary) { this.glossary = glossary; }

    /**
     * Returns a quick, evidence-backed answer for FAQs that do not need a hosted model.
     * A null result means the request needs model synthesis or stronger evidence.
     */
    public String quickAnswer(String message, String language, String websiteContext, String memoryContext) {
        if (message == null || message.isBlank()) return null;
        boolean vi = "vi".equalsIgnoreCase(language);
        String productAnswer = productHelpAnswer(message, vi, websiteContext);
        if (productAnswer != null) return productAnswer;
        Classification classification = classifier.classify(message);
        Intent intent = classification.intent();
        if (isFollowUp(message)) return null;

        if (intent == Intent.CAPABILITIES) return capabilitiesAnswer(vi);

        if (intent == Intent.GENERAL && asksForDefinition(message)) {
            GlossaryService.Definition definition = glossary.find(message);
            if (definition != null) return definitionText(definition, vi, "basic");
        }

        if (classification.confidence() < 0.20) return null;
        boolean portfolioFaq = switch (intent) {
            case PROFILE, SKILLS, PROJECTS, EDUCATION, EXPERIENCE, CONTACT, CV -> true;
            default -> false;
        };
        if (!portfolioFaq && intent != Intent.GENERAL) return null;

        String combinedContext = websiteContext == null ? "" : websiteContext.trim();
        String safeMemory = memoryContext == null ? "" : memoryContext.trim();
        if (!safeMemory.isBlank()) combinedContext += "\nUser-authorized memory reference: " + safeMemory;
        List<KnowledgeRetrievalService.Passage> passages = retriever.retrieve(message, combinedContext, intent, 4);
        double threshold = portfolioFaq ? 0.045 : 0.075;
        List<KnowledgeRetrievalService.Passage> relevant = passages.stream().filter(p -> p.score() >= threshold).toList();
        if (relevant.isEmpty()) {
            if (intent == Intent.GENERAL) {
                GlossaryService.Definition mentioned = glossary.findMentioned(message).stream().findFirst().orElse(null);
                if (mentioned != null) return definitionText(mentioned, vi, "basic");
            }
            return null;
        }
        return groundedAnswer(relevant, intent, vi);
    }

    public String chat(String message, String language, String websiteContext, String memoryContext) {
        return chat(message, language, websiteContext, memoryContext, "");
    }

    public String chat(String message, String language, String websiteContext, String memoryContext, String history) {
        boolean vi = "vi".equalsIgnoreCase(language);
        String productAnswer = productHelpAnswer(message, vi, websiteContext);
        if (productAnswer != null) return productAnswer;
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
                ? "Chào bạn! Mình là MoteLona. Bạn muốn trò chuyện hay tìm hiểu điều gì?"
                : "Hi! I’m MoteLona. What would you like to talk about or find out?";
        if (intent == Intent.THANKS) return vi ? "Rất vui được hỗ trợ bạn! Bạn có thể hỏi tiếp về nội dung Portfolio." : "You’re welcome! Feel free to ask another question about the portfolio.";
        if (intent == Intent.CAPABILITIES) return capabilitiesAnswer(vi);

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
                ? "Mình chưa tìm thấy thông tin này trong dữ liệu Portfolio hiện có. Bạn có thể bật Tìm web để kiểm tra thêm nguồn công khai hoặc mở mục Liên hệ/CV trên trang."
                : "I could not find this in the current Portfolio data. Turn on Web search for public sources or open the Contact/CV section.";
        if (intent != Intent.GENERAL) return vi
                ? "Mình chưa tìm thấy thông tin về " + intentLabel(intent, true) + " trong dữ liệu Portfolio hiện có. Bạn có thể bật Tìm web để kiểm tra thêm nguồn công khai."
                : "I could not find information about " + intentLabel(intent, false) + " in the current Portfolio data. Turn on Web search to check public sources.";

        GlossaryService.Definition mentioned = glossary.findMentioned(message).stream().findFirst().orElse(null);
        if (mentioned != null) return definitionText(mentioned, vi, "basic");
        return vi
                ? "Mình chưa tìm thấy đoạn liên quan trên Portfolio. Bạn có thể hỏi rộng hơn hoặc bật Tìm web nếu câu hỏi cần nguồn mới."
                : "I could not find a related passage on the Portfolio. Try rephrasing, or turn on Web search if the question needs fresh sources.";
    }

    private String productHelpAnswer(String message, boolean vi, String websiteContext) {
        if (message == null || message.isBlank()) return null;
        String q = TextVectorizer.normalize(message);
        if (containsAny(q, "cach su dung motelona", "huong dan su dung", "goi y cau hoi", "nen hoi gi",
                "motelona co the giup gi", "what can i ask", "what can motelona help me with",
                "how do i use", "how to use", "what should i ask")) {
            return vi
                    ? "Bạn có thể hỏi về hồ sơ, kỹ năng, dự án, học vấn, kinh nghiệm hoặc liên hệ. Hãy thử “Có những dự án nào?” hay “MoteLona làm được gì?”. Muốn giải thích thuật ngữ, bôi đen đoạn văn rồi chọn Giải thích; chỉ bật Tìm web khi cần nguồn mới."
                    : "Ask about the profile, skills, projects, education, experience, or contact details. Try “Which projects are listed?” or “What can MoteLona do?”. To explain a term, select it on the page and choose Explain; enable Web search only when you need current public sources.";
        }
        if (containsAny(q, "quyen rieng tu cua chat", "du lieu chat", "luu lich su chat", "luu tin nhan",
                "motelona co luu tin nhan", "motelona co luu lich su", "motelona xu ly du lieu chat",
                "does motelona save my chat", "how does motelona handle chat data", "data privacy",
                "do you store my chat", "do you store my messages", "is my chat saved", "are my messages saved",
                "where does my message go", "who can see my chat", "is my data sent")) {
            return vi
                    ? "Lịch sử chat và bộ nhớ dài hạn có lựa chọn riêng trong Quyền riêng tư & bộ nhớ. Lịch sử chỉ được lưu khi bạn bật; bộ nhớ chỉ lưu khi bạn bật và chủ động yêu cầu ghi nhớ. Nếu có API model, câu hỏi cùng phần trang/tài liệu liên quan có thể được gửi tới provider để tạo câu trả lời. Đừng gửi mật khẩu hoặc khóa API."
                    : "Chat history and long-term memory have separate controls under Privacy & memory. History is saved only when enabled; memory is saved only when enabled and you explicitly ask MoteLona to remember something. If a model API is configured, your question and relevant page/project evidence may be sent to that provider. Do not share passwords or API keys.";
        }
        if (containsAny(q, "du lieu trong database", "database portfolio co gi", "database portfolio co nhung du lieu nao",
                "database portfolio hien co nhung du lieu gi", "database co nhung gi", "du lieu mongodb", "du lieu trong mongo",
                "mongodb dang luu gi", "portfolio database", "du lieu portfolio", "what data is in the database",
                "what data is in the portfolio database", "what is in the database", "what does the database contain",
                "what is stored in mongodb", "what data is in mongodb", "what data does motelona use",
                "what portfolio data is stored", "what does motelona store")) {
            return portfolioDatabaseAnswer(websiteContext, vi);
        }
        if (containsAny(q, "api free", "free api", "api mien phi", "het quota", "he quota",
                "provider loi", "model loi", "api cham", "api slow", "quota limit")) {
            return vi
                    ? "MoteLona tìm câu trả lời trong Portfolio và tài liệu cục bộ trước; câu FAQ có căn cứ không cần gọi model. Câu cần tổng hợp mới dùng provider đã cấu hình, với giới hạn thời gian chung cho cả lượt gọi. Nếu provider lỗi hoặc hết thời gian, MoteLona dùng câu trả lời cục bộ khi có hoặc báo bạn thử lại. Hạn mức free tùy provider và tài khoản."
                    : "MoteLona checks the Portfolio and local project files first, so supported FAQs do not need a model call. Questions that need synthesis use a configured provider under a shared time budget. If providers fail or run out of time, MoteLona uses a local answer when available or asks you to try again. Free limits depend on the provider and account.";
        }
        return null;
    }

    private String portfolioDatabaseAnswer(String websiteContext, boolean vi) {
        Map<String, Integer> counts = new HashMap<>();
        if (websiteContext != null) {
            for (String line : websiteContext.split("\\R")) {
                String prefix = "PORTFOLIO_COUNTS:";
                if (!line.startsWith(prefix)) continue;
                for (String pair : line.substring(prefix.length()).split(";")) {
                    String[] fields = pair.trim().split("=", 2);
                    if (fields.length != 2) continue;
                    try { counts.put(fields[0].trim().toLowerCase(Locale.ROOT), Integer.parseInt(fields[1].trim())); }
                    catch (NumberFormatException ignored) { /* Ignore malformed count fields. */ }
                }
                break;
            }
        }
        String currentCounts = counts.containsKey("profile")
                ? (vi
                    ? " Số liệu công khai hiện có: " + number(counts, "profile", "hồ sơ", "hồ sơ") + ", "
                        + number(counts, "education", "bản ghi học vấn", "bản ghi học vấn") + ", "
                        + number(counts, "experience", "hoạt động/kinh nghiệm", "hoạt động/kinh nghiệm") + ", "
                        + number(counts, "skills", "kỹ năng", "kỹ năng") + ", "
                        + number(counts, "projects", "dự án công khai", "dự án công khai") + " và "
                        + number(counts, "activeknowledge", "mục tri thức AI hoạt động", "mục tri thức AI hoạt động") + "."
                    : " Current public data: " + number(counts, "profile", "profile", "profiles") + ", "
                        + number(counts, "education", "education record", "education records") + ", "
                        + number(counts, "experience", "activity/experience record", "activity/experience records") + ", "
                        + number(counts, "skills", "skill", "skills") + ", "
                        + number(counts, "projects", "published project", "published projects") + ", and "
                        + number(counts, "activeknowledge", "active AI-knowledge item", "active AI-knowledge items") + ".")
                : "";
        return vi
                ? "MoteLona đọc dữ liệu Portfolio công khai trong MongoDB: hồ sơ, học vấn, hoạt động/kinh nghiệm, kỹ năng và dự án đã đăng. Các FAQ không cần thiết gửi dữ liệu này tới model bên ngoài." + currentCounts
                    + " Lịch sử chat và bộ nhớ dài hạn là tính năng riêng, chỉ được lưu khi bạn bật."
                : "MoteLona reads public Portfolio records from MongoDB: profile, education, activity/experience, skills, and published projects. Common FAQs can use this local data without sending it to a hosted model." + currentCounts
                    + " Chat history and long-term memory are separate features and are saved only when enabled.";
    }

    private String number(Map<String, Integer> counts, String key, String singular, String plural) {
        int value = counts.getOrDefault(key, 0);
        return value + " " + (value == 1 ? singular : plural);
    }

    private String capabilitiesAnswer(boolean vi) {
        return vi
                ? "MoteLona trả lời về hồ sơ, kỹ năng, dự án, học vấn, kinh nghiệm và liên hệ từ Portfolio/tài liệu dự án. Trợ lý cũng giải thích đoạn bạn chọn, tìm nguồn web khi bật, rồi đưa bạn tới mục phù hợp và tô sáng nội dung. FAQ có căn cứ được trả lời cục bộ; câu cần tổng hợp mới gọi model nếu có provider. Thử hỏi “Có những dự án nào?” hoặc bôi đen thuật ngữ rồi chọn Giải thích."
                : "MoteLona answers questions about the profile, skills, projects, education, experience, and contact details using the Portfolio and project files. It can explain selected text, search public sources when enabled, and navigate to a relevant section while highlighting content. FAQs with evidence are answered locally; questions needing synthesis use a model when a provider is configured. Try “Which projects are listed?” or select a term and choose Explain.";
    }

    private boolean containsAny(String text, String... phrases) {
        for (String phrase : phrases) if (text.contains(phrase)) return true;
        return false;
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
                ? "Mình tìm thấy ngữ cảnh liên quan trên website:\n" + matches.stream().map(p -> "• " + p.text()).collect(Collectors.joining("\n")) + "\n\nTừ điển hiện chưa có định nghĩa đủ chắc chắn cho đoạn này. Hãy bật nút Tìm web rồi gửi lại đoạn chữ để xem nguồn Internet."
                : "I found related website context:\n" + matches.stream().map(p -> "• " + p.text()).collect(Collectors.joining("\n")) + "\n\nThe built-in glossary does not yet have a reliable definition for this passage. Enable Search web and send the text again to review Internet sources.";
        return vi
                ? "Mình chưa tìm thấy ngữ cảnh đủ phù hợp để giải thích đoạn này. Bạn có thể hỏi thêm chi tiết hoặc bật Tìm web để tham khảo nguồn công khai."
                : "I could not find enough context to explain this selection. Ask a follow-up question or turn on Web search to check public sources.";
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
