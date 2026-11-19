package com.motelona.assistant.dto;

import com.motelona.assistant.search.SearchProvider.SearchResult;
import java.util.List;

public record ChatResponse(String message, UiAction action, boolean aiPowered, String source, List<SearchResult> webResults) {
    public ChatResponse {
        webResults = webResults == null ? List.of() : List.copyOf(webResults);
    }
}
