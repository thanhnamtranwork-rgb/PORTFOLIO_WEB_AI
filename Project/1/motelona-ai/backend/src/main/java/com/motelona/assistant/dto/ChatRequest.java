package com.motelona.assistant.dto;

import java.util.List;

public record ChatRequest(
        String sessionId,
        String message,
        String language,
        String websiteContext,
        List<MessageTurn> history,
        Boolean historyEnabled,
        Boolean memoryEnabled,
        Boolean searchWeb
) { }
