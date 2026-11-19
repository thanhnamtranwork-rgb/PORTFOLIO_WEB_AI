package com.motelona.assistant.dto;

public record MemoryRequest(String sessionId, String content, Boolean consent) { }
