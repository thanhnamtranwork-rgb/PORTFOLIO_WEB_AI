package com.motelona.assistant.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Nearest-example intent classifier using TF-IDF/cosine similarity plus explicit phrase rules. */
public class IntentClassifier {
    public enum Intent { GREETING, THANKS, CAPABILITIES, PROFILE, SKILLS, PROJECTS, EDUCATION, EXPERIENCE, CONTACT, CV, GENERAL }
    public record Classification(Intent intent, double confidence) { }
    private final Map<Intent, List<String>> examples = new EnumMap<>(Intent.class);
    private final List<String> corpus = new ArrayList<>();

    public IntentClassifier() {
        add(Intent.GREETING, "xin chao", "chao ban", "hello", "hi there", "good morning", "ban khoe khong");
        add(Intent.THANKS, "cam on", "cam on ban", "thank you", "thanks a lot");
        add(Intent.CAPABILITIES, "ban co the lam gi", "ban biet lam gi", "what can you do", "how can you help me",
                "chuc nang cua tro ly", "motelona co the lam gi", "mote lona lam gi", "what does motelona do",
                "what can motelona do", "what is motelona", "motelona features", "motelona capabilities",
                "what features does motelona have", "motelona co nhung chuc nang gi", "cac chuc nang cua motelona",
                "motelona ho tro gi", "how does motelona work", "motelona hoat dong nhu the nao",
                "does motelona browse the web", "does motelona search the internet", "ban co tim web khong");
        add(Intent.PROFILE, "gioi thieu ve ban than", "chu nhan website la ai", "who owns this portfolio", "tell me about the portfolio owner", "ten cua toi la gi");
        add(Intent.SKILLS, "ky nang cua toi", "cac ky nang va cong nghe", "what skills do you have", "which technologies are listed", "technology stack", "programming languages");
        add(Intent.PROJECTS, "cac du an cua toi", "du an nao co tren trang", "ke ve project", "what projects are shown", "show me the projects", "project portfolio");
        add(Intent.EDUCATION, "qua trinh hoc tap", "hoc van cua toi", "ban hoc o truong nao", "where did you study", "education background", "major and degree");
        add(Intent.EXPERIENCE, "kinh nghiem lam viec", "lich su lam viec", "ban da lam viec o dau", "work experience", "previous jobs", "internship experience");
        add(Intent.CONTACT, "lien he voi chu nhan", "email lien he", "cach lien lac", "how can i contact you", "contact information", "email address");
        add(Intent.CV, "xem cv cua ban", "tai cv", "resume", "curriculum vitae", "download your cv", "link cv");
    }

    public Classification classify(String text) {
        String normalized = TextVectorizer.normalize(text);
        Intent rule = explicitIntent(normalized);
        if (rule != null) return new Classification(rule, 1.0);
        Intent bestIntent = Intent.GENERAL;
        double best = 0.0;
        List<Double> scores = TextVectorizer.similarities(text, corpus);
        int sampleIndex = 0;
        for (Map.Entry<Intent, List<String>> entry : examples.entrySet()) {
            for (String ignored : entry.getValue()) {
                double score = scores.get(sampleIndex++);
                if (score > best) { best = score; bestIntent = entry.getKey(); }
            }
        }
        return best >= 0.20 ? new Classification(bestIntent, best) : new Classification(Intent.GENERAL, best);
    }

    private Intent explicitIntent(String q) {
        if (contains(q, "xin chao", "chao ban", "hello", "hi ", "good morning", "good evening")) return Intent.GREETING;
        if (contains(q, "cam on", "thank you", "thanks")) return Intent.THANKS;
        if (contains(q, "ban co the lam gi", "ban biet lam gi", "chuc nang cua ban", "chuc nang cua motelona",
                "motelona co the lam gi", "motelona lam gi", "mote lona lam gi", "what can you do",
                "what does motelona do", "what can motelona do", "what is motelona", "motelona features",
                "motelona capabilities", "what features does motelona have", "motelona co nhung chuc nang gi",
                "cac chuc nang cua motelona", "motelona ho tro gi", "how does motelona work",
                "motelona hoat dong nhu the nao", "does motelona browse the web",
                "does motelona search the internet", "ban co tim web khong", "how can you help")) return Intent.CAPABILITIES;
        if (contains(q, "ky nang", "skills", "cong nghe su dung", "technology stack", "programming language")) return Intent.SKILLS;
        if (contains(q, "du an", "projects", "project nao", "san pham da lam")) return Intent.PROJECTS;
        if (contains(q, "hoc van", "hoc tap", "education", "truong dai hoc", "hoc o truong", "hoc truong", "hoc nganh", "major")) return Intent.EDUCATION;
        if (contains(q, "kinh nghiem", "experience", "internship", "noi lam viec")) return Intent.EXPERIENCE;
        if (contains(q, "lien he", "contact", "email", "so dien thoai")) return Intent.CONTACT;
        if (contains(q, "cv", "resume", "curriculum vitae", "ho so xin viec")) return Intent.CV;
        if (contains(q, "gioi thieu", "ban than", "chu nhan", "who are you", "who is the owner", "ten cua ban")) return Intent.PROFILE;
        return null;
    }

    private void add(Intent intent, String... phrases) {
        List<String> items = List.of(phrases);
        examples.put(intent, items);
        corpus.addAll(items);
    }
    private boolean contains(String text, String... phrases) {
        for (String phrase : phrases) if (text.contains(phrase)) return true;
        return false;
    }
}
