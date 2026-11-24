package com.motelona.assistant.service;

import com.motelona.assistant.ai.TextVectorizer;
import com.motelona.assistant.dto.UiAction;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Routes only explicit requests to fixed, known pages and section IDs. */
@Service
public class ActionRouter {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern QUOTED = Pattern.compile("[\"'“”‘’](.{2,120}?)[\"'“”‘’]");
    private static final Pattern TOPIC = Pattern.compile("(?iu)(?:about|regarding|concerning|on|v[eề]|n[oó]i v[eề]|li[eê]n quan [dđ][eế]n|[dđ][eề] c[aậ]p [dđ][eế]n)\\s+(.+)$");
    private static final Pattern SECTION = Pattern.compile("(?m)^SECTION #([A-Za-z0-9-]+):\\s*(.*?)(?=\\R\\RSECTION #|\\z)", Pattern.DOTALL);
    private static final Map<String, String[]> SECTION_TERMS = Map.ofEntries(
        Map.entry("hero", new String[]{"home", "portfolio", "trang chu", "dau trang", "trang chinh"}),
        // Avoid matching the ordinary preposition "about" in requests like "the paragraph about MongoDB".
        Map.entry("about", new String[]{"about me", "about section", "about myself", "gioi thieu", "ban than", "ve toi"}),
        Map.entry("education", new String[]{"education", "hoc van", "truong hoc"}),
        Map.entry("skills", new String[]{"skills", "skill", "ky nang", "cong nghe"}),
        Map.entry("projects", new String[]{"projects", "project", "du an"}),
        Map.entry("stats", new String[]{"stats", "statistics", "thong ke", "con so"}),
        Map.entry("contact", new String[]{"contact", "lien he", "email"}),
        Map.entry("motelona", new String[]{"motelona", "assistant detail", "project detail", "trang chi tiet"}),
        Map.entry("overview", new String[]{"overview", "project overview", "tong quan", "tong quan du an"}),
        Map.entry("features", new String[]{"features", "capabilities", "main functions", "chuc nang", "kha nang"}),
        Map.entry("architecture", new String[]{"architecture", "system architecture", "java architecture", "kien truc", "so do kien truc"}),
        Map.entry("knowledge", new String[]{"glossary", "nlp basics", "concepts", "thuat ngu", "tu dien", "kien thuc nlp"}),
        Map.entry("database", new String[]{"database", "data and api", "mongodb", "mongo db", "co so du lieu", "du lieu va api"}),
        Map.entry("quality", new String[]{"quality", "engineering quality", "testing", "kiem thu", "chat luong"}),
        Map.entry("scope", new String[]{"scope", "limitations", "current scope", "pham vi", "gioi han", "han che"}),
        Map.entry("roadmap", new String[]{"roadmap", "next steps", "learning path", "development roadmap", "lo trinh", "buoc tiep theo"})
    );

    public UiAction routeExplicitNavigation(String userText) {
        return routeExplicitNavigation(userText, "");
    }

    public UiAction routeExplicitNavigation(String userText, String websiteContext) {
        String text = normalize(userText);
        boolean navigational = containsAny(text,
                "take me to", "go to", "navigate to", "scroll to", "jump to", "open the", "show me the section",
                "bring me to", "where can i find", "take me to the paragraph", "cuon den", "dua toi den",
                "chuyen den", "mo phan", "den phan", "di den", "hien phan", "tim cho toi doan",
                "dua toi den doan", "chuyen den doan", "den doan noi ve");
        if (!navigational) return null;

        String topic = extractTopic(userText);
        String bestTarget = null;
        int bestMatchLength = 0;
        for (Map.Entry<String, String[]> entry : SECTION_TERMS.entrySet()) {
            for (String term : entry.getValue()) {
                String normalizedTerm = normalize(term);
                if (normalizedTerm.length() >= bestMatchLength && containsPhrase(text, normalizedTerm)) {
                    bestTarget = entry.getKey();
                    bestMatchLength = normalizedTerm.length();
                }
            }
        }
        if (bestTarget == null) bestTarget = bestContextSection(topic, websiteContext);
        if (bestTarget == null) return null;
        if (topic.isBlank()) topic = bestTarget.equals("motelona") ? "MoteLona" : bestTarget;
        return UiAction.navigate(bestTarget, topic.length() > 120 ? topic.substring(0, 120).trim() : topic);
    }

    public String navigationLabel(String target, String language) {
        if ("vi".equals(language)) return switch (target) {
            case "hero" -> "trang chủ"; case "about" -> "giới thiệu"; case "education" -> "học vấn";
            case "skills" -> "kỹ năng"; case "projects" -> "dự án"; case "stats" -> "thống kê";
            case "contact" -> "liên hệ"; case "motelona" -> "trang chi tiết MoteLona";
            case "overview" -> "tổng quan"; case "features" -> "chức năng";
            case "architecture" -> "kiến trúc"; case "knowledge" -> "kiến thức NLP"; case "database" -> "cơ sở dữ liệu và API";
            case "quality" -> "chất lượng kỹ thuật"; case "scope" -> "phạm vi và giới hạn"; case "roadmap" -> "lộ trình";
            default -> "mục";
        };
        return switch (target) {
            case "hero" -> "home"; case "about" -> "about"; case "education" -> "education";
            case "skills" -> "skills"; case "projects" -> "projects"; case "stats" -> "statistics";
            case "contact" -> "contact"; case "motelona" -> "the MoteLona detail page";
            case "overview" -> "project overview"; case "features" -> "capabilities";
            case "architecture" -> "architecture"; case "knowledge" -> "NLP concepts"; case "database" -> "database and API";
            case "quality" -> "engineering quality"; case "scope" -> "scope and limitations"; case "roadmap" -> "roadmap";
            default -> "section";
        };
    }

    private String bestContextSection(String topic, String context) {
        if (context == null || context.isBlank() || topic == null || topic.isBlank()) return null;
        List<String> sections = new ArrayList<>();
        Matcher matcher = SECTION.matcher(context);
        while (matcher.find()) sections.add(matcher.group(1) + "\n" + matcher.group(2));
        if (sections.isEmpty()) return null;
        List<Double> similarities = TextVectorizer.similarities(topic, sections);
        List<String> queryTokens = TextVectorizer.tokens(topic);
        List<ScoredSection> scored = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            String section = sections.get(i);
            double score = 0.72 * similarities.get(i)
                    + 0.28 * TextVectorizer.overlap(queryTokens, TextVectorizer.tokens(section));
            scored.add(new ScoredSection(section.substring(0, section.indexOf('\n')), score));
        }
        return scored.stream()
                .filter(section -> section.score() >= 0.10)
                .max(Comparator.comparingDouble(ScoredSection::score))
                .map(ScoredSection::id)
                .orElse(null);
    }

    private String extractTopic(String userText) {
        if (userText == null || userText.isBlank()) return "";
        Matcher quoted = QUOTED.matcher(userText);
        if (quoted.find()) return cleanTopic(quoted.group(1));
        Matcher topic = TOPIC.matcher(userText.trim());
        if (topic.find()) return cleanTopic(topic.group(1));
        return "";
    }

    private String cleanTopic(String value) {
        if (value == null) return "";
        String result = value.replaceAll("[.!?;,:]+$", "").trim();
        return result.length() > 120 ? result.substring(0, 120).trim() : result;
    }

    private String normalize(String input) {
        if (input == null) return "";
        String lower = input.toLowerCase(Locale.ROOT).replace('đ', 'd');
        return DIACRITICS.matcher(Normalizer.normalize(lower, Normalizer.Form.NFD)).replaceAll("")
                .replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) if (containsPhrase(text, normalize(needle))) return true;
        return false;
    }

    private boolean containsPhrase(String text, String term) {
        return !term.isBlank() && (" " + text + " ").contains(" " + term + " ");
    }

    private record ScoredSection(String id, double score) { }
}
