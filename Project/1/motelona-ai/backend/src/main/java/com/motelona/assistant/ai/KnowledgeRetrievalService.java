package com.motelona.assistant.ai;

import com.motelona.assistant.ai.IntentClassifier.Intent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Retrieves grounded passages from page text and the MySQL portfolio knowledge base. */
public class KnowledgeRetrievalService {
    public record Passage(String text, double score) { }

    public List<Passage> retrieve(String query, String context, Intent intent, int limit) {
        if (query == null || query.isBlank() || context == null || context.isBlank()) return List.of();
        List<String> chunks = split(context);
        if (chunks.isEmpty()) return List.of();
        String expandedQuery = query + " " + intentTerms(intent);
        List<Passage> matches = new ArrayList<>();
        for (String chunk : chunks) {
            double tfidf = TextVectorizer.similarity(expandedQuery, chunk, chunks);
            double overlap = TextVectorizer.overlap(expandedQuery, chunk);
            double score = (tfidf * 0.72) + (overlap * 0.28) + sectionBoost(chunk, intent);
            if (score > 0.035) matches.add(new Passage(chunk, score));
        }
        return matches.stream().sorted(Comparator.comparingDouble(Passage::score).reversed())
                .limit(Math.max(1, Math.min(limit, 8))).toList();
    }

    private List<String> split(String context) {
        List<String> chunks = new ArrayList<>();
        for (String line : context.split("\\R+")) {
            String clean = line.trim();
            if (clean.isBlank()) continue;
            if (clean.length() > 1600) {
                for (String part : clean.split("(?<=[.;])\\s+|;\\s*")) addChunk(chunks, part);
            } else addChunk(chunks, clean);
        }
        return chunks.stream().distinct().toList();
    }

    private void addChunk(List<String> chunks, String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.length() > 1100) clean = clean.substring(0, 1100);
        if (!clean.isBlank()) chunks.add(clean);
    }

    private double sectionBoost(String text, Intent intent) {
        String normalized = TextVectorizer.normalize(text).toLowerCase(Locale.ROOT);
        String marker = switch (intent) {
            case PROFILE -> "profile";
            case SKILLS -> "skills";
            case PROJECTS -> "projects";
            case EDUCATION -> "education";
            case EXPERIENCE -> "experience";
            case CONTACT, CV -> "profile";
            default -> "";
        };
        return !marker.isBlank() && normalized.startsWith(marker) ? 0.24 : 0.0;
    }

    private String intentTerms(Intent intent) {
        return switch (intent) {
            case PROFILE -> "profile full name headline about career objective owner biography";
            case SKILLS -> "skills technologies technology skill proficiency category";
            case PROJECTS -> "projects title description technology status demo source code";
            case EDUCATION -> "education school university major degree study";
            case EXPERIENCE -> "experience organization position work internship";
            case CONTACT -> "contact email location linkedin github website";
            case CV -> "profile cv resume curriculum vitae cv url";
            default -> "";
        };
    }
}
