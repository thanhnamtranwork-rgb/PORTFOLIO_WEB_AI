package com.motelona.assistant.controller;

import com.motelona.assistant.ai.LocalAiEngine;
import com.motelona.assistant.dto.ChatRequest;
import com.motelona.assistant.dto.ChatResponse;
import com.motelona.assistant.dto.ExplainRequest;
import com.motelona.assistant.dto.WebsiteSearchRequest;
import com.motelona.assistant.search.SearchProvider;
import com.motelona.assistant.service.AiChatService;
import com.motelona.assistant.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiChatService aiChatService;
    private final RateLimitService rateLimit;
    private final SearchProvider searchProvider;

    public AiController(AiChatService aiChatService, RateLimitService rateLimit, SearchProvider searchProvider) {
        this.aiChatService = aiChatService;
        this.rateLimit = rateLimit;
        this.searchProvider = searchProvider;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.ofEntries(
                Map.entry("backend", "up"),
                Map.entry("model", "ready"),
                Map.entry("provider", "java-local"),
                Map.entry("engine", "self-built-nlp"),
                Map.entry("engineVersion", LocalAiEngine.ENGINE_VERSION),
                Map.entry("apiKeyRequired", false),
                Map.entry("apiKeyConfigured", false),
                Map.entry("message", "MoteLona uses local Java NLP without an external AI API key. Explicit web searches use DuckDuckGo's public HTML results page and require outbound server Internet access."),
                Map.entry("internetSearchEnabled", searchProvider.isEnabled()),
                Map.entry("internetSearchProvider", "DuckDuckGo HTML (no API key)"));
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        rateLimit.check("ai-chat-minute", ip, 12, Duration.ofMinutes(1));
        rateLimit.check("ai-chat-day", ip, 100, Duration.ofDays(1));
        return aiChatService.chat(request);
    }

    @PostMapping("/explain")
    public ChatResponse explain(@RequestBody ExplainRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        rateLimit.check("ai-explain-minute", ip, 20, Duration.ofMinutes(1));
        rateLimit.check("ai-explain-day", ip, 150, Duration.ofDays(1));
        return aiChatService.explain(request);
    }

    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody WebsiteSearchRequest request, HttpServletRequest http) {
        rateLimit.check("ai-search", http.getRemoteAddr(), 30, Duration.ofMinutes(1));
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        var results = aiChatService.searchWebsite(request.query(), request.websiteContext());
        return Map.of("query", request.query(), "source", "website", "results", results, "count", results.size());
    }

    @GetMapping("/search/web")
    public ResponseEntity<Map<String, Object>> searchWeb(@RequestParam String query, HttpServletRequest http) {
        rateLimit.check("ai-web-search", http.getRemoteAddr(), 10, Duration.ofMinutes(1));
        if (query == null || query.isBlank()) throw new IllegalArgumentException("Search query is required.");
        if (query.length() > 200) throw new IllegalArgumentException("Search query must be 200 characters or fewer.");
        if (!searchProvider.isEnabled()) return ResponseEntity.ok(Map.of("enabled", false, "source", "web-disabled",
                "message", "Internet search is unavailable in this deployment.", "results", List.of()));
        return ResponseEntity.ok(Map.of("enabled", true, "source", "duckduckgo-html", "results", searchProvider.search(query)));
    }
}
