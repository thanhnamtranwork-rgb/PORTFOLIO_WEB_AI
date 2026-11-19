package com.motelona.assistant.ai;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Small, dependency-free TF-IDF and cosine-similarity implementation for local retrieval. */
public final class TextVectorizer {
    private static final Pattern WORDS = Pattern.compile("[^a-z0-9]+", Pattern.CASE_INSENSITIVE);
    private static final Set<String> STOP_WORDS = Set.of(
            "va", "la", "cua", "toi", "minh", "ban", "anh", "chi", "em", "co", "cac", "nhung",
            "nao", "gi", "the", "nay", "do", "kia", "mot", "nhieu", "hay", "hoac", "voi", "cho",
            "ve", "trong", "tren", "duoc", "khong", "thi", "ma", "se", "da", "dang", "rat", "that",
            "how", "what", "which", "where", "when", "who", "is", "are", "a", "an", "to",
            "of", "for", "in", "on", "at", "and", "or", "does", "did", "can", "could", "would",
            "you", "your", "me", "my", "i", "we", "they", "it", "this", "please", "tell"
    );

    private TextVectorizer() { }

    public static String normalize(String input) {
        if (input == null) return "";
        String lower = input.toLowerCase(Locale.ROOT).replace('đ', 'd');
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();
    }

    public static List<String> tokens(String input) {
        String normalized = normalize(input);
        if (normalized.isBlank()) return List.of();
        List<String> raw = WORDS.splitAsStream(normalized)
                .filter(token -> token.length() > 1 && !STOP_WORDS.contains(token))
                .toList();
        List<String> result = new ArrayList<>(raw);
        // Add selected bigrams so phrases such as "ky nang" and "du an" retain meaning.
        for (int i = 0; i + 1 < raw.size(); i++) result.add(raw.get(i) + "_" + raw.get(i + 1));
        return result;
    }

    public static double similarity(String query, String document, List<String> corpus) {
        List<String> queryTokens = tokens(query);
        List<String> documentTokens = tokens(document);
        if (queryTokens.isEmpty() || documentTokens.isEmpty()) return 0.0;

        Map<String, Integer> documentFrequency = new HashMap<>();
        for (String item : corpus == null ? List.<String>of() : corpus) {
            Set<String> unique = new HashSet<>(tokens(item));
            unique.forEach(token -> documentFrequency.merge(token, 1, Integer::sum));
        }
        int documentCount = Math.max(1, corpus == null ? 1 : corpus.size());
        Map<String, Double> queryVector = tfIdf(queryTokens, documentFrequency, documentCount);
        Map<String, Double> documentVector = tfIdf(documentTokens, documentFrequency, documentCount);
        return cosine(queryVector, documentVector);
    }

    public static double overlap(String query, String document) {
        Set<String> q = new HashSet<>(tokens(query));
        Set<String> d = new HashSet<>(tokens(document));
        if (q.isEmpty() || d.isEmpty()) return 0.0;
        q.retainAll(d);
        return (double) q.size() / Math.sqrt(tokens(query).size() * (double) tokens(document).size());
    }

    private static Map<String, Double> tfIdf(List<String> tokens, Map<String, Integer> df, int n) {
        Map<String, Integer> counts = new HashMap<>();
        tokens.forEach(token -> counts.merge(token, 1, Integer::sum));
        Map<String, Double> vector = new HashMap<>();
        counts.forEach((token, count) -> {
            double tf = 1.0 + Math.log(count);
            double idf = Math.log(1.0 + (n + 1.0) / (1.0 + df.getOrDefault(token, 0)));
            vector.put(token, tf * idf);
        });
        return vector;
    }

    private static double cosine(Map<String, Double> left, Map<String, Double> right) {
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        double dot = 0.0, leftNorm = 0.0, rightNorm = 0.0;
        for (double value : left.values()) leftNorm += value * value;
        for (double value : right.values()) rightNorm += value * value;
        for (Map.Entry<String, Double> entry : left.entrySet()) {
            dot += entry.getValue() * right.getOrDefault(entry.getKey(), 0.0);
        }
        if (leftNorm == 0.0 || rightNorm == 0.0) return 0.0;
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
