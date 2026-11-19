package com.motelona.assistant.dto;

public record ExplainRequest(
        String sessionId,
        String term,
        String selectedText,
        String level,
        String language,
        String websiteContext
) { }
