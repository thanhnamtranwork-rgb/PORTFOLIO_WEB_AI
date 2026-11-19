package com.motelona.assistant.service;

import com.motelona.assistant.ai.TextVectorizer;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Local TF-IDF/cosine text retrieval over supplied page content. */
@Service
public class LocalSearchService {
    public List<Match> search(String query, String context) {
        if (query == null || query.isBlank() || context == null || context.isBlank()) return List.of();
        List<String> chunks = new ArrayList<>();
        for (String chunk : context.split("(?<=[.!?;])\\s+|\\R+")) {
            String clean = chunk.trim();
            if (!clean.isEmpty()) chunks.add(clean.substring(0, Math.min(clean.length(), 900)));
        }
        if (chunks.isEmpty()) return List.of();
        List<Match> matches = new ArrayList<>();
        for (String chunk : chunks) {
            double score = 0.72 * TextVectorizer.similarity(query, chunk, chunks)
                    + 0.28 * TextVectorizer.overlap(query, chunk);
            if (score > 0.015) matches.add(new Match(chunk, (int) Math.round(score * 1000)));
        }
        return matches.stream().sorted(Comparator.comparingInt(Match::score).reversed()).limit(8).toList();
    }
    public record Match(String snippet, int score) { }
}
