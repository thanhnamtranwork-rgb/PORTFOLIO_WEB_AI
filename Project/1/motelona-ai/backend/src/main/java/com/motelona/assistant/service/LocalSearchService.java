package com.motelona.assistant.service;

import com.motelona.assistant.ai.TextVectorizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Local TF-IDF/cosine text retrieval over supplied page content and project documents. */
@Service
public class LocalSearchService {
    public List<Match> search(String query, String context) {
        if (query == null || query.isBlank() || context == null || context.isBlank()) return List.of();
        return rank(query, split(context), null).stream().map(RankedPassage::match).toList();
    }

    /** Ranks passages from all source files in one corpus pass and retains each source name. */
    public List<SourceMatch> searchDocuments(String query, Map<String, String> documents) {
        if (query == null || query.isBlank() || documents == null || documents.isEmpty()) return List.of();
        List<Passage> passages = new ArrayList<>();
        documents.forEach((source, content) -> split(content).forEach(item -> passages.add(new Passage(source, item.text()))));
        String normalized = TextVectorizer.normalize(query);
        String preferred = normalized.contains("readme") ? "README.md" : null;
        return rank(query, passages, preferred).stream()
                .map(row -> new SourceMatch(row.passage().source(), row.match())).toList();
    }

    private List<RankedPassage> rank(String query, List<Passage> passages, String preferredSource) {
        if (passages.isEmpty()) return List.of();
        String expandedQuery = expandQuery(query);
        String normalizedQuery = TextVectorizer.normalize(query);
        boolean projectIntent = isProjectIntent(normalizedQuery);
        List<String> chunks = passages.stream().map(Passage::text).toList();
        List<Double> similarities = TextVectorizer.similarities(expandedQuery, chunks);
        List<String> queryTokens = TextVectorizer.tokens(expandedQuery);
        List<RankedPassage> matches = new ArrayList<>();
        for (int i = 0; i < passages.size(); i++) {
            Passage passage = passages.get(i);
            List<String> passageTokens = TextVectorizer.tokens(passage.text());
            double score = 0.72 * similarities.get(i) + 0.28 * TextVectorizer.overlap(queryTokens, passageTokens);
            String normalizedChunk = TextVectorizer.normalize(passage.text());
            if (projectIntent && (normalizedChunk.contains("published project")
                    || normalizedChunk.contains("projects") || normalizedChunk.contains("project:"))) score += 0.16;
            if (preferredSource != null && preferredSource.equalsIgnoreCase(passage.source())) score += 3.0;
            if (score > 0.015) matches.add(new RankedPassage(passage,
                    new Match(passage.text(), (int) Math.round(score * 1000))));
        }
        return matches.stream().sorted(Comparator.comparingInt((RankedPassage row) -> row.match().score()).reversed())
                .limit(12).toList();
    }

    private List<Passage> split(String context) {
        List<Passage> passages = new ArrayList<>();
        if (context == null || context.isBlank()) return passages;
        for (String chunk : context.split("(?<=[.!?;])\\s+|\\R+")) {
            String clean = chunk.trim();
            if (!clean.isEmpty()) passages.add(new Passage("", clean.substring(0, Math.min(clean.length(), 900))));
        }
        return passages;
    }

    private String expandQuery(String query) {
        String normalized = TextVectorizer.normalize(query);
        StringBuilder expanded = new StringBuilder(query);
        if (isProjectIntent(normalized)) expanded.append(" projects project portfolio featured title names descriptions published work");
        if (containsAny(normalized, "ky nang", "skill", "technology", "tech stack", "cong nghe")) {
            expanded.append(" skills technical skills technologies programming languages tools stack");
        }
        if (containsAny(normalized, "hoc van", "education", "university", "truong")) {
            expanded.append(" education university school major degree academic");
        }
        if (containsAny(normalized, "gioi thieu", "ban than", "about", "who am i", "profile")) {
            expanded.append(" about profile personal introduction biography");
        }
        return expanded.toString();
    }

    private boolean isProjectIntent(String normalizedQuery) {
        return containsAny(normalizedQuery, "du an", "project", "projects", "portfolio work", "show my work",
                "what have i built", "what did i build", "what have you built");
    }

    private boolean containsAny(String value, String... phrases) {
        for (String phrase : phrases) if ((" " + value + " ").contains(" " + phrase + " ")) return true;
        return false;
    }

    public record Match(String snippet, int score) { }
    public record SourceMatch(String source, Match match) { }
    private record Passage(String source, String text) { }
    private record RankedPassage(Passage passage, Match match) { }
}
