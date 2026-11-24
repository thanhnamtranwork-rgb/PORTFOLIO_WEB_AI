package com.motelona.assistant.service;

public record ModelPrompt(String systemInstruction, String userPrompt, int maxOutputTokens) { }
